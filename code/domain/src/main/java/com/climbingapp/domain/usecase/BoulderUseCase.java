package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.HoldDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoulderUseCase {

    BoulderDTO createBoulder(BoulderDTO boulderDTO, List<HoldDTO> holds);

    BoulderDTO getBoulderById(int id);

    List<HoldDTO> getBoulderHolds(int boulderId);

    Page<BoulderDTO> getAllBoulders(int gymId, Pageable pageable);

    Page<BoulderDTO> getBouldersByWallImage(int wallImageId, Pageable pageable);

    BoulderDTO updateBoulder(BoulderDTO boulderDTO);

    void deleteBoulder(int id);
}
