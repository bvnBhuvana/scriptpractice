package com.slokam.scriptone.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.slokam.scriptone.exception.ApplicationException;
import com.slokam.scriptone.exception.DataNotFoundException;

import lombok.extern.slf4j.Slf4j;

import com.slokam.scriptone.dto.SceneCriteriaDTO;

import com.slokam.scriptone.entity.*;
@Slf4j
@Service
public class ScriptCriteriaImpl {

	@Autowired
	private EntityManager entityManager;
	
	

	public List<Scene> getScenes2(SceneCriteriaDTO sceneCriteriaDTO)
			throws DataNotFoundException,ApplicationException,Exception {
		CriteriaBuilder criteriaBuilder=entityManager.getCriteriaBuilder();
		CriteriaQuery<Scene> criteriaQuery= criteriaBuilder.createQuery(Scene.class);
		
		Root<Scene> rootScene = criteriaQuery.from(Scene.class);
		
		Join<Scene, Location> locationJoin = rootScene.join("location");
		Join<Scene,Script> scriptJoin=rootScene.join("script");
	    
		
		List<Predicate> predicates=new ArrayList<>();	
		if(sceneCriteriaDTO.getScriptId()!=null) {
		 predicates.add(criteriaBuilder.equal(scriptJoin.get("id"),sceneCriteriaDTO.getScriptId()));
		}
		if(sceneCriteriaDTO.getLocationName()!=null && !sceneCriteriaDTO.getLocationName().isEmpty()) {
		 predicates.add(criteriaBuilder.equal(locationJoin.get("name") ,sceneCriteriaDTO.getLocationName()));
		}
      
                                      
		//criteriaQuery.select(rootScene).where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        return entityManager.createQuery(criteriaQuery).getResultList();
	}

	
	public List<Scene> getScenes(SceneCriteriaDTO sceneCriteriaDTO)
			throws DataNotFoundException,ApplicationException,Exception {CriteriaBuilder criteriaBuilder=entityManager.getCriteriaBuilder();
			
			CriteriaQuery<Scene> criteriaQuery= criteriaBuilder.createQuery(Scene.class);
			
			Root<Dialogue> rootDialogue = criteriaQuery.from(Dialogue.class);
			
			Join<Dialogue, Scene> sceneJoin = rootDialogue.join("scene");
		
			Join<Dialogue, ScriptCharector> scriptCharectorJoin = rootDialogue.join("scriptCharector");
			
			List<Predicate> predicates=new ArrayList<>();	
			if(sceneCriteriaDTO.getScriptCharectorName()!=null && !sceneCriteriaDTO.getScriptCharectorName().isEmpty()) {
			  predicates.add(criteriaBuilder.equal(scriptCharectorJoin.get("name"),sceneCriteriaDTO.getScriptCharectorName()));
			}
			
	      
	                                      
			//criteriaQuery.select(rootScene).where(criteriaBuilder.and(predicates.toArray(new Predicate[0])));
	        criteriaQuery.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
	        return entityManager.createQuery(criteriaQuery).getResultList();
	        }
	
	
	public List<Scene> getScenes4(SceneCriteriaDTO sceneCriteriaDTO)
			throws DataNotFoundException,ApplicationException,Exception {
		CriteriaBuilder criteriaBuilder=entityManager.getCriteriaBuilder();
		CriteriaQuery<Scene> criteriaQuery = criteriaBuilder.createQuery(Scene.class);
		Root<Scene> rootScene=criteriaQuery.from(Scene.class);
		
		CriteriaQuery<Scene> subquery = criteriaBuilder.createQuery(Scene.class);
		Root<Dialogue> dialogueRoot = subquery.from(Dialogue.class);
		
		Join<Scene, Location> locationJoin=rootScene.join("location");
		Join<Scene,Script> scriptJoin=rootScene.join("script");
	    Join<Dialogue,Scene> dialogueJoin =dialogueRoot .join("scene");
	        
	    Join<Dialogue, ScriptCharector> scriptCharectorJoin = dialogueRoot.join("scriptCharector");
	   
		
		List<Predicate> predicates=new ArrayList<>();	
		if(sceneCriteriaDTO.getScriptId()!=null) {
		 predicates.add(criteriaBuilder.equal(scriptJoin.get("id"),sceneCriteriaDTO.getScriptId()));
		}
		if(sceneCriteriaDTO.getLocationName()!=null && !sceneCriteriaDTO.getLocationName().isEmpty()) {
		 predicates.add(criteriaBuilder.equal(locationJoin.get("name") ,sceneCriteriaDTO.getLocationName()));
		}
        if(sceneCriteriaDTO.getScriptCharectorName()!=null && !sceneCriteriaDTO.getScriptCharectorName().isEmpty()) {
        	predicates.add(criteriaBuilder.equal(scriptCharectorJoin.get("name"),sceneCriteriaDTO.getScriptCharectorName()));
        	
        
         }
        List<Scene> combinedResult = new ArrayList<>();
        CriteriaQuery<Scene> secnes=criteriaQuery.select(rootScene).where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        CriteriaQuery<Scene> scenes1= criteriaQuery.select(dialogueJoin).distinct(true).where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        List<Scene> result1 = entityManager.createQuery(secnes).getResultList();
        List<Scene> result2 = entityManager.createQuery(scenes1).getResultList();
//        combinedResult.addAll(result1);
//        combinedResult.addAll(result2);
        
        for (Scene scene : result1) {
            if (result2.contains(scene)) {
                combinedResult.add(scene);
            }
        }
     // return entityManager.createQuery(criteriaQuery).getResultList();
           return  combinedResult;
	}
	


	public List<ScriptCharector> findScriptCharactersByScene(Long sceneId) {
        String jpql = "SELECT st FROM Dialogue d " +
                     "JOIN d.scriptCharector st " +
                     "JOIN d.scene s " +
                     "WHERE s.id =:sceneId";

        Query query = entityManager.createQuery(jpql, ScriptCharector.class);
         query.setParameter("sceneId",sceneId);

        return query.getResultList();
    }
//  List<Scene> combinedResult = Stream.concat(result1.stream(), result2.stream())
//  .collect(Collectors.toList());

	public List<Scene> findScenesByScriptCharacter1(String scriptCharacter) {

		 
		        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		        CriteriaQuery<Scene> criteriaQuery = criteriaBuilder.createQuery(Scene.class);
		        
		      Root<Dialogue> dialogueRoot = criteriaQuery.from(Dialogue.class);
		   //  Root<Scene> rootScene=criteriaQuery.from(Scene.class);
		       
		        Join<Dialogue,Scene> dialogueJoin =dialogueRoot .join("scene");
		        
		        Join<Dialogue, ScriptCharector> scriptCharectorJoin = dialogueRoot.join("scriptCharector");
		        
		      
		      // criteriaQuery.select(rootScene).distinct(true).where(criteriaBuilder.equal(scriptCharectorJoin.get("name"), scriptCharacter));
		       criteriaQuery.select(dialogueJoin).distinct(true).where(criteriaBuilder.equal(scriptCharectorJoin.get("name"), scriptCharacter));
			      
		        return entityManager.createQuery(criteriaQuery).getResultList();
	}	  
	

////	SELECT DISTINCT s
////	FROM Scene s
////	JOIN Dialogue d ON d.scene = s
////	JOIN ScriptCharector sc ON d.scriptCharector = sc
////	WHERE sc.name = :param0
//	
//
	public List<Scene> findScenesByScriptCharacter(SceneCriteriaDTO sceneCriteriaDTO) {

		 
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Scene> criteriaQuery = criteriaBuilder.createQuery(Scene.class);
        
      Root<Dialogue> dialogueRoot = criteriaQuery.from(Dialogue.class);
   //  Root<Scene> rootScene=criteriaQuery.from(Scene.class);
       
        Join<Dialogue,Scene> dialogueJoin =dialogueRoot .join("scene");
        
        Join<Dialogue, ScriptCharector> scriptCharectorJoin = dialogueRoot.join("scriptCharector");
        
      
      // criteriaQuery.select(rootScene).distinct(true).where(criteriaBuilder.equal(scriptCharectorJoin.get("name"), scriptCharacter));
       criteriaQuery.distinct(true).where(criteriaBuilder.equal(scriptCharectorJoin.get("name"), sceneCriteriaDTO.getScriptCharectorName()));
	      
        return entityManager.createQuery(criteriaQuery).getResultList();
}
	
}


