package com.tritronik.gcp.laundry.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tritronik.gcp.laundry.model.Shedlock;

public interface ShedlockRepo extends JpaRepository<Shedlock, String>{
	
}
