package com.neratzis.bookstore.repository;

import com.neratzis.bookstore.model.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface MusicRepository extends JpaRepository<Music,Long> {

    Page<Music> findByArtistContainingIgnoreCaseAndDeletedFalse(String artist, Pageable pageable);

    Page<Music> findByProductionCompanyAndDeletedFalse(String productionCompany, Pageable pageable);

    Page<Music> findAllByFeaturedTrueAndDeletedFalse(Pageable pageable);

}
