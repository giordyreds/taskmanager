package it.uniroma3.siw.taskmanager.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User {
//DICHIARAZIONE VARIABILI
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(nullable = false, length = 100)
	private String firstName;
	
	@Column(nullable = false, length = 100)
	private String lastName;
	
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "owner")
	private List<Project> ownedProjects;
	
	@ManyToMany(mappedBy = "members")
	private List<Project> visibleProjects;
	
	@Column(updatable = false, nullable = false)
	private LocalDateTime creationTimeStamp;
	
	@Column(nullable = false)
	private LocalDateTime lastUpdateTimeStamp;
	
	//COSTRUTTORI E METODI
	
	public User() {
		this.ownedProjects = new ArrayList<>();
		this.visibleProjects = new ArrayList<>();
	}
	
	public User(String firstName, String lastName) {
		this();
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	@PrePersist
	protected void onPersist() {
		this.creationTimeStamp = LocalDateTime.now();
		this.lastUpdateTimeStamp = LocalDateTime.now();
	}
	
	@PreUpdate
	protected void onUpdate() {
		this.lastUpdateTimeStamp = LocalDateTime.now();
	}
	//GETTERS E SETTERS
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public List<Project> getOwnedProjects() {
		return ownedProjects;
	}

	public void setOwnedProjects(List<Project> ownedProjects) {
		this.ownedProjects = ownedProjects;
	}

	public List<Project> getVisibleProjects() {
		return visibleProjects;
	}

	public void setVisibleProjects(List<Project> visibleProjects) {
		this.visibleProjects = visibleProjects;
	}

	public LocalDateTime getCreationTimeStamp() {
		return creationTimeStamp;
	}

	public void setCreationTimeStamp(LocalDateTime creationTimeStamp) {
		this.creationTimeStamp = creationTimeStamp;
	}

	public LocalDateTime getLastUpdateTimeStamp() {
		return lastUpdateTimeStamp;
	}

	public void setLastUpdateTimeStamp(LocalDateTime lastUpdateTimeStamp) {
		this.lastUpdateTimeStamp = lastUpdateTimeStamp;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", creationTimeStamp="
				+ creationTimeStamp + ", lastUpdateTimeStamp=" + lastUpdateTimeStamp + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
		result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (firstName == null) {
			if (other.firstName != null)
				return false;
		} else if (!firstName.equals(other.firstName))
			return false;
		if (lastName == null) {
			if (other.lastName != null)
				return false;
		} else if (!lastName.equals(other.lastName))
			return false;
		return true;
	}
	
	
	
}
