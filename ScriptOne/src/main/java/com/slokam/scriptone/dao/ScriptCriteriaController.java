package com.slokam.scriptone.dao;

import java.util.List;

import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


import org.springframework.web.bind.annotation.RestController;

import com.slokam.scriptone.dto.SceneCriteriaDTO;
import com.slokam.scriptone.entity.Scene;
import com.slokam.scriptone.entity.ScriptCharector;
import com.slokam.scriptone.exception.ApplicationException;
import com.slokam.scriptone.exception.DataNotFoundException;
@RequestMapping("/criteria")
@RestController
public class ScriptCriteriaController {
	@Autowired
	private ScriptCriteriaImpl serviceCriteria;
	@GetMapping("/getallscenes")
	public ResponseEntity<List<Scene>> getScenesList(@RequestBody SceneCriteriaDTO  userInput)
			throws DataNotFoundException,ApplicationException,Exception {
		 List<Scene> scenesList= serviceCriteria.getScenes(userInput);
		 if(scenesList.isEmpty()) {
			 return ResponseEntity.notFound().build();
		 }
		 
		 return ResponseEntity.status(HttpStatus.OK).body(scenesList);
	}
	@GetMapping("/getchar/{SceneId}")
	public ResponseEntity<List<ScriptCharector>> getAllChars(@PathVariable Long SceneId){
		List<ScriptCharector> charList=serviceCriteria.findScriptCharactersByScene(SceneId);
		if(charList.isEmpty()) {
			 return ResponseEntity.notFound().build();
		 }
		 
		 return ResponseEntity.status(HttpStatus.OK).body(charList);
		
	}
	@GetMapping("/getlistofscenes")
	public ResponseEntity<List<Scene>> getScenesListByChar(@RequestBody SceneCriteriaDTO sceneCriteriaDTO){
		
		 List<Scene> scenesList= serviceCriteria.findScenesByScriptCharacter(sceneCriteriaDTO);
		 if(scenesList.isEmpty()) {
			 return ResponseEntity.notFound().build();
		 }
		 
		 return ResponseEntity.status(HttpStatus.OK).body(scenesList);
		
		
		
	}
}
// Long scriptId, String locationName,String scriptChar