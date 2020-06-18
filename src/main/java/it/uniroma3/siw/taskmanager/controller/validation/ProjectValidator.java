package it.uniroma3.siw.taskmanager.controller.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import it.uniroma3.siw.taskmanager.model.Project;
import it.uniroma3.siw.taskmanager.service.ProjectService;

@Component
public class ProjectValidator implements Validator {
	
	final Integer MAX_NAME_LENGTH = 100;
	final Integer MIN_NAME_LENGTH = 2;
	final Integer MAX_DESCRIPTION_LENGTH = 1000;
	
	@Autowired
	ProjectService projectService;
	
	@Override
	public void validate(Object o, Errors errors) {
		Project project = (Project) o;
		String name = project.getName().trim();
		String description = project.getDescription().trim();
		
		if(name.isEmpty() || name==null)
			errors.rejectValue("name", "required");
		else if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH)
			errors.rejectValue("name", "size");
		
		if (description.length() > MAX_DESCRIPTION_LENGTH)
			errors.rejectValue("description", "size");
		}
	
	@Override
	public boolean supports (Class<?> clazz) {return Project.class.equals(clazz);}

}
