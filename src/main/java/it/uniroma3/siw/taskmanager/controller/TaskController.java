package it.uniroma3.siw.taskmanager.controller;

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
import it.uniroma3.siw.taskmanager.controller.validation.TaskValidator;
import it.uniroma3.siw.taskmanager.model.Credentials;
import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.model.Task;
import it.uniroma3.siw.taskmanager.model.User;
import it.uniroma3.siw.taskmanager.service.CredentialsService;
import it.uniroma3.siw.taskmanager.service.ProjectService;
import it.uniroma3.siw.taskmanager.service.TaskService;

@Controller
public class TaskController {
	
	//il TaskController é suddiviso in 5 metodi che gestiscono le operazioni sulle task dei progetti posseduti da un utente
	
	@Autowired
	ProjectService projectService;
	
	@Autowired
	SessionData sessionData;
	
	@Autowired
	TaskValidator taskValidator;
	
	@Autowired
	TaskService taskService;
	
	@Autowired
	CredentialsValidator credentialsValidator;
	
	@Autowired
	CredentialsService credentialsService;
	
	//1-createTaskForm permette all'utente loggato di creare una nuova task mostrando la relativa form
	@RequestMapping(value = {"/projects/{projectId}/tasks/add"}, method  = RequestMethod.GET)
	public String createTaskForm(Model model, @PathVariable("projectId") Long projectId) {
		User loggedUser = sessionData.getLoggedUser();
		model.addAttribute("loggedUser", loggedUser);
		model.addAttribute("currentProject", projectService.getProject(projectId));
		model.addAttribute("taskForm", new Task());
		return "addTask";
	}
	
	//2-createTask effettua un controllo sui dati inseriti nella form dall'utente loggato e se non sono corretti lo reindirizza alla form
	//con un messaggio d'errore generato da taskValidator, altrimenti la registrazione della viene effettuata con successo
	//e l'utente viene reindirizzato al progetto a cui appartiene la task
	@RequestMapping(value = {"/projects/{projectId}/tasks/add"}, method  = RequestMethod.POST )
	public String createTask(@Valid @ModelAttribute("taskForm") Task task,
			@PathVariable("projectId") Long projectId,
					BindingResult taskBindingResult,
					Model model){
		User loggedUser = sessionData.getLoggedUser();
		Project project = this.projectService.getProject(projectId);
		taskValidator.validate(task, taskBindingResult);
		if(project !=null) {
			if(!taskBindingResult.hasErrors()) {
				this.taskService.saveTask(task);
				project.addTask(task);
				projectService.saveProject(project);
				return "redirect:/projects/" + project.getId(); //forse non cosi ma come sul project controller
			}
			model.addAttribute("loggedUser",loggedUser);
			return "addTask";
		}
		return "redirect:/projects"; //forse non cosi come sopra ma anche dubbio su / dopo projects
		}
	
	//3-deleteTask rimuove la task associata al progetto posseduto dall'utente loggato
	//se l'utente loggato non é un owner del progetto non puó eliminare la task
	@RequestMapping(value = {"/projects/{projectId}/tasks/delete/{taskId}"}, method = RequestMethod.POST)
	public String deleteTask(Model model, @PathVariable Long projectId, @PathVariable Long taskId) {
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}
		Task task = this.taskService.getTask(taskId);
		project.getTasks().remove(task);
		this.projectService.saveProject(project);
		return "redirect:/projects/{projectId}";
	}
	
	//4-taskForm apre nuovamente la form per aggiornare i dati del progetto
	@RequestMapping(value = {"/projects/{projectId}/tasks/update/{taskId}"}, method = RequestMethod.GET)
	public String taskForm(Model model, @PathVariable Long projectId, @PathVariable Long taskId) {
		Task task = taskService.getTask(taskId);
		Project project = projectService.getProject(projectId);
		if(!project.getOwner().equals(sessionData.getLoggedUser())) {
			return "redirect:/projects/";
		}
		model.addAttribute("projectForm", project);
		model.addAttribute("taskForm", task);
		return "taskUpdate";
	}
	
	//5-updateTask effettua il controllo dei dati inseriti nella task che si vuole aggiornare
	@RequestMapping(value = {"/projects/{projectId}/tasks/update/{taskId}"}, method = RequestMethod.POST)
	public String updateTask(@Valid @ModelAttribute("taskForm") Task taskForm, @PathVariable Long taskId, 
							 @PathVariable Long projectId, BindingResult taskBindingResult, Model model) {
		
		User loggedUser = sessionData.getLoggedUser();
		Project project = projectService.getProject(projectId);
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}
		taskValidator.validate(taskForm, taskBindingResult);
		if(!taskBindingResult.hasErrors()) {
			Task task = this.taskService.getTask(taskId);
			task.setDescription(taskForm.getDescription());
			task.setName(taskForm.getName());
			task.setCompleted(taskForm.getCompleted());
			this.taskService.saveTask(task);
			return "taskUpdateSuccessful";
		}
		return "taskUpdate";
	}
	
	//6-assignTask assegna una task ad un utente con cui condivide il progetto a cui appartiene la task
	//NB:se l'utente con cui si vuole condividere la task non esiste viene mostrata una pagina di errore
	@RequestMapping(value= { "/projects/{projectId}/tasks/assign/{taskId}" }, method = RequestMethod.GET)
	public String assignTask(Model model, @PathVariable Long projectId, @PathVariable Long taskId) {
		Project project = projectService.getProject(projectId);
		if(!project.getOwner().equals(sessionData.getLoggedUser())) {
			return "redirect:/projects";
		}
		model.addAttribute("credentialsForm", new Credentials());
		return "assignTask";
	}

	//7-assignForm controlla se lo username inserito appartiene ad un utente giá registrato, altrimenti comunica un errore
	@RequestMapping(value= { "/projects/{projectId}/tasks/assign/{taskId}" }, method = RequestMethod.POST)
	public String assignForm(Model model, @Valid @ModelAttribute ("credentialsForm") Credentials credentialsForm, BindingResult credentialsBindingResult, @PathVariable Long projectId,  @PathVariable Long taskId) {
		User loggedUser = sessionData.getLoggedUser();
		System.out.println("ID PATH POST: " + projectId);
		Project project = projectService.getProject(projectId);
		System.out.println("ID PROJECT POST: " + project.getId());
		if(!project.getOwner().equals(loggedUser)) {
			return "redirect:/projects";
		}
		credentialsValidator.checkUserNameEntered(credentialsForm, credentialsBindingResult);
		if(!credentialsBindingResult.hasErrors()) {
			credentialsValidator.refersToProjectMember(credentialsForm, project, credentialsBindingResult);
			if(!credentialsBindingResult.hasErrors()) {
				Credentials credentials = this.credentialsService.getCredentials(credentialsForm.getUserName());
				User user= credentials.getUser();
				Task task = this.taskService.getTask(taskId);
				task.setAssigned(user);
				this.taskService.saveTask(task);
				return "taskAssignSuccessful";
			}
		}
		return "assignTask";
	}
	
	
	
	
	
	
	
}
