package com.tritronik.gcp.laundry.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(indexes = { @Index(name = "idx_locked_by", columnList = "lockedBy", unique = false) })
public class Shedlock {
	@Id
	private String name;

	private String lockedBy;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lockedAt;

	@Temporal(TemporalType.TIMESTAMP)
	private Date lockUntil;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public Date getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(Date lockedAt) {
		this.lockedAt = lockedAt;
	}

	public Date getLockUntil() {
		return lockUntil;
	}

	public void setLockUntil(Date lockUntil) {
		this.lockUntil = lockUntil;
	}

	@Override
	public String toString() {
		return "Shedlock [name=" + name + ", lockedBy=" + lockedBy + ", lockedAt=" + lockedAt + ", lockUntil="
				+ lockUntil + "]";
	}

}
