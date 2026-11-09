package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.GymStaffDTO;
import com.climbingapp.domain.dto.GymStaffMemberDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.dto.UserGymRoleDTO;
import com.climbingapp.domain.exception.ForbiddenException;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.GymUseCase;
import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.climbingapp.infrastructure.entity.enums.GymRole;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
public class GymUseCaseImpl implements GymUseCase {

    private static final int LOGO_SIZE_PX = 512;

    @Autowired private GymRepository gymRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private GymStaffRepository gymStaffRepository;

    @Autowired private ImageProcessorUseCase imageProcessorUseCase;

    @Autowired private Clock clock;

    @Override
    public GymDTO createGym(GymDTO gymDTO) {
        try {
            validateTimeZone(gymDTO.getTimeZone());
            gymDTO.setCreatedDate(Instant.now(clock));
            if (gymDTO.getLogo() != null) {
                gymDTO.setLogo(resizeLogo(gymDTO.getLogo()));
            }
            return gymRepository.save(gymDTO);
        } catch (Exception e) {
            log.error("Error creating gym: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public GymDTO getGymById(int id) {
        try {
            return gymRepository.findById(id);
        } catch (Exception e) {
            log.error("Error getting gym by id: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Page<GymDTO> getAllGyms(Pageable pageable) {
        try {
            return gymRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error getting gyms: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Copies the request onto the stored gym so creator, creation date and logo survive. Name, time
     * zone and logo are kept when not sent; location and description are replaced as sent.
     */
    @Override
    public GymDTO updateGym(GymDTO newGymDTO) {
        try {
            GymDTO currentGym = gymRepository.findById(newGymDTO.getId());
            if (currentGym == null) {
                return null;
            }
            if (newGymDTO.getTimeZone() != null) {
                validateTimeZone(newGymDTO.getTimeZone());
                currentGym.setTimeZone(newGymDTO.getTimeZone());
            }
            if (newGymDTO.getName() != null) {
                currentGym.setName(newGymDTO.getName());
            }
            if (newGymDTO.getLogo() != null) {
                currentGym.setLogo(resizeLogo(newGymDTO.getLogo()));
            }
            currentGym.setLocation(newGymDTO.getLocation());
            currentGym.setDescription(newGymDTO.getDescription());
            currentGym.setUpdatedDate(Instant.now(clock));
            return gymRepository.save(currentGym);
        } catch (Exception e) {
            log.error("Error updating gym: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteGymById(int id) {
        try {
            gymRepository.deleteById(id);
        } catch (Exception e) {
            log.error("Error deleting gym by id: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<GymStaffMemberDTO> getGymStaff(int gymId) {
        requireGym(gymId);
        return gymStaffRepository.findByGymId(gymId).stream()
                .map(staff -> toMember(staff, userRepository.findById(staff.getUserId())))
                .toList();
    }

    @Override
    public GymStaffMemberDTO addGymStaff(
            int gymId, String email, GymRole role, int actorId, boolean actorIsAdmin) {
        requireGym(gymId);
        UserDTO user = userRepository.findByEmail(email.trim().toLowerCase());
        if (user == null) {
            throw new NotFoundException("No user is registered with that email.");
        }
        GymStaffDTO staff = gymStaffRepository.findByGymIdAndUserId(gymId, user.getId());
        if (!actorIsAdmin
                && (role == GymRole.OWNER || (staff != null && staff.getRole() == GymRole.OWNER))) {
            throw new ForbiddenException("Only an admin can grant or change the owner role.");
        }
        if (staff == null) {
            staff = new GymStaffDTO();
            staff.setGymId(gymId);
            staff.setUserId(user.getId());
        }
        staff.setRole(role);
        staff.setAssignedBy(actorId);
        staff.setAssignedAt(Instant.now(clock));
        GymStaffDTO saved = gymStaffRepository.save(staff);
        log.info("User {} is now {} of gym {} (by user {})", user.getId(), role, gymId, actorId);
        return toMember(saved, user);
    }

    @Override
    public void removeGymStaff(int gymId, int userId, boolean actorIsAdmin) {
        GymStaffDTO staff = gymStaffRepository.findByGymIdAndUserId(gymId, userId);
        if (staff == null) {
            throw new NotFoundException("That user is not staff of this gym.");
        }
        if (!actorIsAdmin && staff.getRole() == GymRole.OWNER) {
            throw new ForbiddenException("Only an admin can remove a gym owner.");
        }
        gymStaffRepository.deleteById(staff.getId());
        log.info("User {} removed from the staff of gym {}", userId, gymId);
    }

    @Override
    public List<UserGymRoleDTO> getGymRoles(int userId) {
        return gymStaffRepository.findByUserId(userId).stream()
                .map(
                        staff -> {
                            GymDTO gym = gymRepository.findById(staff.getGymId());
                            return new UserGymRoleDTO(
                                    staff.getGymId(),
                                    gym != null ? gym.getName() : null,
                                    staff.getRole());
                        })
                .toList();
    }

    private void requireGym(int gymId) {
        if (gymRepository.findById(gymId) == null) {
            throw new NotFoundException("Gym with id " + gymId + " does not exist.");
        }
    }

    private static GymStaffMemberDTO toMember(GymStaffDTO staff, UserDTO user) {
        return new GymStaffMemberDTO(
                staff.getUserId(),
                user != null ? user.getName() : null,
                user != null ? user.getEmail() : null,
                staff.getRole(),
                staff.getAssignedAt());
    }

    private byte[] resizeLogo(byte[] logo) {
        return imageProcessorUseCase.resize(logo, LOGO_SIZE_PX, LOGO_SIZE_PX);
    }

    /**
     * Only IANA region ids (e.g. Europe/Madrid) are accepted. Fixed offsets such as +01:00 are
     * rejected because they don't follow daylight saving time.
     */
    private static void validateTimeZone(String timeZone) {
        if (timeZone == null || !ZoneId.getAvailableZoneIds().contains(timeZone)) {
            throw new IllegalArgumentException(
                    "A valid IANA time zone is required (e.g. Europe/Madrid).");
        }
    }
}
