package it.uniroma3.siw.taskmanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Transactional
	public User getUser(Long id) {
		Optional<User> result = this.userRepository.findById(id);
		return result.orElse(null);
	}
	
	/*@Transactional
	public User getUser(String username) {
		Optional<User> result = this.userRepository.findByUserName(username);
		return result.orElse(null);
	}*/
	
	@Transactional
	public User saveUser(User user) {
		return this.userRepository.save(user);
	}
	
	@Transactional
	public List<User> findAllUsers() {
		Iterable<User> iterable = this.userRepository.findAll();
		ArrayList<User> lista = new ArrayList<>();
		for(User user : iterable) {
			lista.add(user);
		}
		
		return lista;
	}
    
	@Transactional
	public List<User> getMembers(Project project) {
		List<User> iterable = this.userRepository.findByVisibleProjects(project);
		List<User> result = new ArrayList<>();
		for(User user: iterable)
			result.add(user);
		return result;
	}
}
