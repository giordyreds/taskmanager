package it.uniroma3.siw.taskmanager.model;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

@Entity
public class Credentials {

	public static final String DEFAULT_ROLE = "DEFAULT";
	public static final String ADMIN_ROLE = "ADMIN";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique = true, nullable = false, length = 100)
	private String userName;
	
	@Column(nullable = false, length = 100)
	private String password;
	
	@OneToOne(cascade = CascadeType.ALL)
	private User user;
	
	@Column(updatable = false, nullable = false)
    private LocalDateTime creationTimestamp;

    @Column(nullable = false)
    private LocalDateTime lastUpdateTimestamp;	
    
    @Column(nullable = false, length = 10)
	private String role;
    
    public Credentials() {
    }
    
    public Credentials(String userName, String password) {
    	this();
    	this.userName = userName;
    	this.password = password;
    }
    
    @PrePersist
    protected void onPersist() {
        this.creationTimestamp = LocalDateTime.now();
        this.lastUpdateTimestamp = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdateTimestamp = LocalDateTime.now();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getCreationTimestamp() {
		return creationTimestamp;
	}

	public void setCreationTimestamp(LocalDateTime creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}

	public LocalDateTime getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}

	public void setLastUpdateTimestamp(LocalDateTime lastUpdateTimestamp) {
		this.lastUpdateTimestamp = lastUpdateTimestamp;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Credentials)) return false;
        Credentials user = (Credentials) o;
        return Objects.equals(userName, user.userName) &&
                Objects.equals(role, user.role) &&
                Objects.equals(creationTimestamp, user.creationTimestamp) &&
                Objects.equals(lastUpdateTimestamp, user.lastUpdateTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, role);
    }

	@Override
	public String toString() {
		return "Credentials [id=" + id + ", userName=" + userName + ", creationTimestamp=" + creationTimestamp
				+ ", lastUpdateTimestamp=" + lastUpdateTimestamp + ", role=" + role + "]";
	}
    
    
    
}
