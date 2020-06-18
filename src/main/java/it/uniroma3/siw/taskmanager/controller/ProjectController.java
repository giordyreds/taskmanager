package it.uniroma3.siw.taskmanager.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import it.uniroma3.siw.taskmanager.controller.session.SessionData;
import it.uniroma3.siw.taskmanager.controller.validation.CredentialsValidator;
import it.uniroma3.siw.taskmanager.controller.validation.ProjectValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.repository.ProjectRepository;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.UserService;

@Controller
public class ProjectController {
	
	//il ProjectController é suddiviso in 10 metodi che gestiscono le operazioni sui progetti degli utenti
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	CredentialsService credentialsService;
	
	@Autowired
	ProjectValidator projectValidator;
	
	@Autowired
	CredentialsValidator credentialsValidator;
	
	@Autowired
	SessionData sessionData;
	
	//1-myOwnedProjects tramite il projectService risale ai progetti posseduti solo ed esclusivamente dall'utente che ha effettuato il login
	@RequestMapping(value = {"/projects"}, method = RequestMethod.GET)
	public String myOwnedProjects(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		List<Project> projectList = projectService.retrieveProjectOwned(loggedUser);
		model.addAttribute("loggedUser", loggedUser );
		model.addAttribute("projectList", projectList);
		return "myOwnedProjects";
	}
	
	//2-project si occupa della vista del progetto e delle task associate al progetto
	//infatti in project.html abbiamo implementato la vista del progetto e le relative task
	@RequestMapping(value = {"/projects/{projectId}"}, method = RequestMethod.GET)
	public String project(Model model,
			              @PathVariable Long projectId) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		if (project == null)
			return "redirect:/projects";
		List<User> members = userService.getMembers(project);
		if (!project.getOwner().equals(loggedUser) && !members.contains(loggedUser))
			return "redirect:/projects";
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("project", project);
		model.addAttribute("members", members);
		return "project";
	}
	
	//3-createProjectForm permette all'utente loggato di creare un nuovo progetto mostrando la relativa form
	@RequestMapping(value = {"/projects/add"}, method = RequestMethod.GET)
	public String createProjectForm(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("projectForm", new Project());
		return "addProject";
	}
	
	//4-createProject effettua un controllo sui dati inseriti nella form dall'utente loggato e se non sono corretti lo reindirizza alla form
	//con un messaggio d'errore generato da projectValidator, altrimenti la registrazione del progetto viene effettuata con successo
	//e l'utente viene reindirizzato al progetto creato
	@RequestMapping(value = {"/projects/add"}, method = RequestMethod.POST)
	public String createProject(@Valid @ModelAttribute("projectForm") Project project,
			                    BindingResult projectBindingResult,
			                    Model model) {
		User loggedUser = sessionData.getLoggedUser();
		
		projectValidator.validate(project, projectBindingResult);
		if (!projectBindingResult.hasErrors()) {
			project.setOwner(loggedUser);
			this.projectService.saveProject(project);
			return "redirect:/projects/" + project.getId();
		}
		model.addAttribute("loggedUser", loggedUser);
		return "addProject";
	}
	
	//5-removeProject tramite il projectService attua un'operazione di delete e reindirizza l'utente loggato alla sua lista di progetti
	@RequestMapping(value= {"projects/{projectId}/delete"}, method = RequestMethod.POST)
	public String removeProject(Model model, @PathVariable Long projectId) {
		Project project = this.projectService.getProject(projectId);
		this.projectService.deleteProject(project);
		return "redirect:/projects";
	}
	
	//6-shareProjectForm permette di condividere un progetto con un altro utente il cui username é presente nel database
	@RequestMapping(value = {"projects/share/{projectId}"}, method = RequestMethod.GET)
	public String shareProjectForm(Model model ,@PathVariable Long projectId) {
		Project project = projectService.getProject(projectId);
		model.addAttribute("projectForm",project);
		model.addAttribute("credentialsForm", new Credentials());
		return "shareProject";
	}
	
	//7-shareProject effettua il controllo dell'esistenza dell'utente con cui si vuole condividere il progetto
	//se il nome utente non esiste reindirizza alla pagina di share
	@RequestMapping(value = {"projects/share/{projectId}"}, method = RequestMethod.POST )
	public String shareProject(Model model, @Valid @ModelAttribute ("credentialsForm") Credentials credentialsForm, 
			BindingResult credentialsBindingResult, @PathVariable Long projectId) {
		
		Project project = projectService.getProject(projectId);
		credentialsValidator.checkUserNameEntered(credentialsForm, credentialsBindingResult);
		if(!credentialsBindingResult.hasErrors()) {
			Credentials credentials = this.credentialsService.getCredentials(credentialsForm.getUserName());
			User user = credentials.getUser();
			this.projectService.shareProjectWithUser(project,  user);
			return "shareSuccess";
		}
		model.addAttribute("projectForm", project);
		model.addAttribute("credentialsForm", credentialsForm);		
		return "shareProject";
	}
	
	//8-projectShareWithMe tramite la repository creata, crea una lista di progetti che i vari utenti hanno condiviso con me
	@RequestMapping(value = {"/sharedWithMe"}, method = RequestMethod.GET)
	public String projectSharedWithMe(Model model) {
		User loggedUser = sessionData.getLoggedUser();
		List<Project> sharedWithMe = this.projectRepository.findByMembers(loggedUser);
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("sharedWithMe", sharedWithMe);
		return "sharedWithMe";
	}
	
	//9-projectForm apre nuovamente la form per aggiornare i dati del progetto
	@RequestMapping(value= { "/projects/update/{projectId}" }, method = RequestMethod.GET)
	public String projectForm(Model model, @PathVariable Long projectId) {
		Project project = projectService.getProject(projectId);
		User loggedUser = sessionData.getLoggedUser();
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects/";
		}
		model.addAttribute("projectForm", project);
		return "projectUpdate";
	}

	//10-updateProject effettua il controllo dei dati inseriti nel progetto che si vuole aggiornare
	@RequestMapping(value = { "/projects/update/{projectId}" }, method = RequestMethod.POST)
	public String updateProject(@Valid @ModelAttribute("projectForm") Project projectForm, @PathVariable Long projectId,
			BindingResult projectBindingResult, Model model) {
		projectValidator.validate(projectForm, projectBindingResult);
		if (!projectBindingResult.hasErrors()) {
			Project project = this.projectService.getProject(projectId);
			project.setDescription(projectForm.getDescription());
			project.setName(projectForm.getName());
			this.projectService.saveProject(project);
			return "projectUpdateSuccessful";
		}
		return "projectUpdate";
	}
	
}
