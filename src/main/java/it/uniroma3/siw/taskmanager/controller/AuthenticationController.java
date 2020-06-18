package it.uniroma3.siw.taskmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.CredentialsValidator;
import it.uniroma3.siw.taskmanager.controller.validation.UserValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;

@Controller
public class AuthenticationController {

	@Autowired
	CredentialsService credentialsService;
	
	@Autowired
	UserValidator userValidator;
	
	@Autowired
	CredentialsValidator credentialsValidator;
	
	@Autowired
	SessionData sessionData;
	
	//L'authentication controller presenta due metodi:
	//1-showRegisterForm: quando viene cliccato sull'href di register reindirizza ad una pagina di registrazione
	//con i campi: username, password, nome e cognome. Crea delle nuove credenziali a cui associa un nuovo user
	@RequestMapping(value = {"/users/register" }, method = RequestMethod.GET)
	public String showRegisterForm(Model model) {
		model.addAttribute("userForm", new User());
		model.addAttribute("credentialsForm", new Credentials());
		
		return "registerUser";
	}
	
	//2-registerUser: questo metodo controlla che i dati inseriti siano validi e reindirizza ad una pagina di accesso al login oppure nuovamente 
	//alla pagina index.html dove é possibile effettuare il login o registrare un altro utente
	@RequestMapping(value = { "/users/register" }, method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("userForm") User user,
				BindingResult userBindingResult,
				@Valid @ModelAttribute("credentialsForm") Credentials credentials,
				BindingResult credentialsBindingResult,
				Model model) {
	
		this.userValidator.validate(user, userBindingResult);
		this.credentialsValidator.validate(credentials, credentialsBindingResult);
	
		if(!userBindingResult.hasErrors() && !credentialsBindingResult.hasErrors()) {
			credentials.setUser(user);
			credentialsService.saveCredentials(credentials);
			return "registrationSuccessful";
		}
	
		return "registerUser";
	
	}
	
}
