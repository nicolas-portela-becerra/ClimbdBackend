package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.GymStaffMemberDTO;
import com.climbingapp.domain.dto.UserGymRoleDTO;
import com.climbingapp.infrastructure.entity.enums.GymRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GymUseCase {

    GymDTO createGym(GymDTO gymDTO);

    GymDTO getGymById(int id);

    Page<GymDTO> getAllGyms(Pageable pageable);

    GymDTO updateGym(GymDTO gymDTO);

    void deleteGymById(int id);

    List<GymStaffMemberDTO> getGymStaff(int gymId);

    /**
     * Adds the user with that email to the gym staff, or changes their role. Only an admin can
     * grant, change or remove the OWNER role.
     */
    GymStaffMemberDTO addGymStaff(
            int gymId, String email, GymRole role, int actorId, boolean actorIsAdmin);

    /** Only an admin can remove an OWNER, so an owner can't remove or demote themselves. */
    void removeGymStaff(int gymId, int userId, boolean actorIsAdmin);

    /** Gyms where the user is OWNER or SETTER. */
    List<UserGymRoleDTO> getGymRoles(int userId);
}
