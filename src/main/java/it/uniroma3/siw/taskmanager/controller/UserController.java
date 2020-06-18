package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.CredentialsValidator;
import it.uniroma3.siw.taskmanager.controller.validation.UserValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.repository.UserRepository;
import it.uniroma3.siw.taskmanager.service.CredentialsService;

@Controller
public class UserController {

	//lo userController é suddiviso in 7 metodi che gestiscono le operazioni degli utenti all'interno del taskmanager
	//in particolare se l'utente possiede il ruolo di admin puó eliminare un altro utente ed in cascata i relativi progetti e task

	@Autowired
	UserRepository userRepository;
	
	@Autowired
    UserValidator userValidator;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    CredentialsService credentialsService;
    
    @Autowired
    CredentialsValidator credentialsValidator;
    
    @Autowired
    SessionData sessionData;
    
    
    //1-home reindirizza al menu principale l'utente appena registrato
    //questo metodo tuttavia viene utilizzato anche per navigare piú agevolmente all'interno del taskmanager
    @RequestMapping(value = {"/home"}, method = RequestMethod.GET)
    public String home(Model model) {
    	User loggedUser = sessionData.getLoggedUser();
    	model.addAttribute("loggedUser", loggedUser);
    	return "home";
    }
    
    //2-me gestisce la vista del profilo dell'utente loggato
    @RequestMapping(value = { "/users/me" }, method = RequestMethod.GET)
    public String me(Model model) {
        User loggedUser = sessionData.getLoggedUser();
        Credentials credentials = sessionData.getLoggedCredentials();
        System.out.println(credentials.getPassword());
        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("credentials", credentials);
        return "userProfile";
    }
    
    //3-admin reindirizza l'utente loggato con il ruolo di admin ad una pagina dove puó eliminare
    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String admin(Model model) {
        User loggedUser = sessionData.getLoggedUser();
        model.addAttribute("loggedUser", loggedUser);
        return "admin";
    }
    
    //4-userList permette la visione di tutti gli utenti registrati da parte dell'admin
    @RequestMapping(value = { "/admin/users" }, method = RequestMethod.GET)
    public String userList(Model model) {
    	User loggedUser = sessionData.getLoggedUser();
    	List<Credentials> allCredentials = this.credentialsService.getAllCredentials();
    	
    	model.addAttribute("loggedUser", loggedUser);
    	model.addAttribute("credentialsList", allCredentials);
    	
    	return "allUsers";
    }
    
    @RequestMapping(value = { "/admin/users/{username}/delete" }, method = RequestMethod.POST)
    public String removeUser(Model model, @PathVariable String username) {
    	this.credentialsService.deleteCredentials(username);
    	return "redirect:/admin/users";
    }
    
	//9-updateForm apre nuovamente la form per aggiornare i dati di registrazione dell'utente
    @RequestMapping(value = {"/users/update"}, method = RequestMethod.GET)
    public String updateForm(Model model) {
    	model.addAttribute("userForm", sessionData.getLoggedUser());
    	model.addAttribute("credentialsForm", new Credentials(sessionData.getLoggedCredentials().getUserName(),null));
    	
    	return "userUpdate";
    }
    
    //10-updateUser effettua il controllo dei dati inseriti nei campi di registrazione che si vogliono aggiornare
    //NB:lo username non puó essere uguale al precedente
    @RequestMapping(value = {"/users/update"}, method = RequestMethod.POST)
    public String updateUser(@Valid @ModelAttribute("userForm") User user,
    						 BindingResult userBindingResult,
    						 @Valid @ModelAttribute("credentialsForm") Credentials credentials,
    						 BindingResult credentialsBindingResult,
    						 Model model) {
    	
    	Credentials cred = credentialsService.getCredentials(sessionData.getLoggedCredentials().getId());
    	
    	this.userValidator.validate(user, userBindingResult);
    	this.credentialsValidator.validate(credentials, credentialsBindingResult);
    	
    	if(!credentials.getPassword().trim().isEmpty() && !credentials.getPassword().equals(null)) {
    		cred.setPassword(this.passwordEncoder.encode(credentials.getPassword()));
    	}
    	cred.setUserName(credentials.getUserName());
    	if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
    		User usr = cred.getUser();
    		usr.setFirstName(user.getFirstName());
    		usr.setLastName(user.getLastName());
    		
    		cred.setUser(usr);
    		this.credentialsService.updateCredentials(cred);
    		
    		cred.setPassword("[PROTECTED]");
			this.sessionData.setLoggedUser(usr);
			this.sessionData.setCredentials(cred);
			
			return "userUpdateSuccessful";
    	}
    	return "userUpdate";    	
    }  
}
