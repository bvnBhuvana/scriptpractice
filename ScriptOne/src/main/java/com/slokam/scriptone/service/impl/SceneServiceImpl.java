package com.slokam.scriptone.service.impl;

import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.internal.build.AllowSysOut;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slokam.scriptone.dao.IActionDAO;
import com.slokam.scriptone.dao.IDialogueDAO;
import com.slokam.scriptone.dao.ILocationDAO;
import com.slokam.scriptone.dao.ISceneDAO;
import com.slokam.scriptone.dao.ISceneElementOrderDAO;
import com.slokam.scriptone.dao.IScriptCharectorDAO;
import com.slokam.scriptone.dto.ActionDTO;
import com.slokam.scriptone.dto.DialogueDTO;
import com.slokam.scriptone.dto.LocationDTO;
import com.slokam.scriptone.dto.SceneDTO;
import com.slokam.scriptone.dto.ScriptCharectorDTO;
import com.slokam.scriptone.entity.Action;
import com.slokam.scriptone.entity.Dialogue;
import com.slokam.scriptone.entity.Location;
import com.slokam.scriptone.entity.Scene;
import com.slokam.scriptone.entity.SceneElementOrder;
import com.slokam.scriptone.entity.ScriptCharector;
import com.slokam.scriptone.exception.ApplicationException;
import com.slokam.scriptone.exception.DataNotFoundException;
import com.slokam.scriptone.service.ISceneService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SceneServiceImpl implements ISceneService {

	@Autowired
	private ILocationDAO locationDao;
	@Autowired
	private ISceneDAO sceneDao;
	@Autowired
	private ModelMapper mapper;

	@Autowired
	private ISceneElementOrderDAO seoDao;

	@Autowired
	private IActionDAO actionDao;

	@Autowired
	private IScriptCharectorDAO scoDao;
	@Autowired
	private IDialogueDAO dialogueDao;

	@Override
	@Transactional
	public SceneDTO saveScene(SceneDTO sceneDTO) throws DataNotFoundException, ApplicationException, Exception {
		Scene scene = mapper.map(sceneDTO, Scene.class);
		Location location = null;
		if (sceneDTO.getLocationDto().getId() != null) {
			Optional<Location> opt = locationDao.findById(sceneDTO.getLocationDto().getId());
			if (!opt.isPresent()) {
				throw new DataNotFoundException("Location is not available");
			}
			location = opt.get();
		} else {
			LocationDTO locationDto = sceneDTO.getLocationDto();
			
			location = locationDao.findByName(locationDto.getName());

			if (location == null) {
				Location newLocation = mapper.map(locationDto, Location.class);
				location = locationDao.save(newLocation);
				locationDto.setId(location.getId());
			}
			
		}
		scene.setLocation(location);
		

		sceneDao.save(scene);
		sceneDTO.setId(scene.getId());
		List<ActionDTO> actionDTOList = sceneDTO.getActionList();

		for (ActionDTO actionDTO : actionDTOList) {
			Action action = mapper.map(actionDTO, Action.class);
			action.setScene(scene);

			SceneElementOrder seo = new SceneElementOrder();
			seo.setScene(scene);
			seo.setSequenceOrderNumber(actionDTO.getSceneElementOrderId());
			seoDao.save(seo);
			action.setSceneElementOrder(seo);
			actionDao.save(action);
			actionDTO.setId(action.getId());
		}
		List<DialogueDTO> dialogueList = sceneDTO.getDialogueList();

		for (DialogueDTO dialogueDTO : dialogueList) {
			ScriptCharectorDTO sco = dialogueDTO.getScDTO();
			ScriptCharector newSc=null;
			ScriptCharector sc = null;
			if (sco.getId() != null) {
				Optional<ScriptCharector> scOptional = scoDao.findById(sco.getId());
				if (!scOptional.isPresent()) {
					throw new DataNotFoundException("Charector is not available.");
				}
				sc = scOptional.get();
				
			} else {
				 newSc= scoDao.findByName(sco.getName());
				 
				if(newSc==null) {
					sc = mapper.map(sco, ScriptCharector.class);
				    sc.setScript(scene.getScript());
				 newSc=scoDao.save(sc);
				 
				}
			}
			SceneElementOrder seo = new SceneElementOrder();
			seo.setScene(scene);
			seo.setSequenceOrderNumber(dialogueDTO.getSceneElementOrderId());
			seoDao.save(seo);
			
			Dialogue dialogue = mapper.map(dialogueDTO, Dialogue.class);
			dialogue.setScene(scene);
			dialogue.setSceneOrder(seo);
			dialogue.setScriptCharector(newSc);
			dialogueDao.save(dialogue);
			dialogueDTO.setId(dialogue.getId());
		}

		log.debug("SCENE DTO ::" + sceneDTO.toString());
		log.debug("SCENE ENTITY ::" + scene.toString());

		//
		return sceneDTO;
	}

	@Override
	public SceneDTO getSceneById(Long id) throws DataNotFoundException, ApplicationException, Exception {
		Optional<Scene> optionalSceneDto = sceneDao.findById(id);
		
		SceneDTO sceneDto =null;
		if (optionalSceneDto.isPresent()) {
			Scene scene = optionalSceneDto.get();
			sceneDto = mapper.map(scene, SceneDTO.class);
			Location location = scene.getLocation();

			LocationDTO locationdto = mapper.map(location, LocationDTO.class);

			sceneDto.setLocationDto(locationdto);

			List<ActionDTO> actionDtoList = new ArrayList<ActionDTO>();
			List<Action> actionList = actionDao.getActions(id);
			for (Action action : actionList) {
				ActionDTO actionDto = mapper.map(action, ActionDTO.class);

				actionDtoList.add(actionDto);

			}
			sceneDto.setActionList(actionDtoList);
			

			List<Dialogue> dialogueList = dialogueDao.getDialogueList(id);
			List<DialogueDTO> dialogueDtoList = new ArrayList<>();

			for (Dialogue dialogue : dialogueList) {
				ScriptCharector scChar = dialogue.getScriptCharector();
				ScriptCharectorDTO scriptCharDto = mapper.map(scChar, ScriptCharectorDTO.class);
				DialogueDTO dialogueDto = mapper.map(dialogue, DialogueDTO.class);
				dialogueDto.setScDTO(scriptCharDto);

				dialogueDtoList.add(dialogueDto);
			}
			sceneDto.setDialogueList(dialogueDtoList);

		}
		return sceneDto;
	}
	@Override
	public List<SceneDTO> getAllScenes()  throws DataNotFoundException, ApplicationException, Exception {
		  
		List<Scene> scenesList= sceneDao.findAll();
		
		List<SceneDTO> getsceneDtoList= new ArrayList();
	
			
		
		SceneDTO sceneDto=null;
		for(Scene scene:scenesList) {
			Location location = scene.getLocation();
			sceneDto=mapper.map(scene,SceneDTO.class);
			LocationDTO locationDto=mapper.map(location,LocationDTO.class);
			sceneDto.setLocationDto(locationDto);
			
			List<Action> allActions=actionDao.findAll();
			List<ActionDTO> allActionDto=Arrays.asList(mapper.map(allActions,ActionDTO[].class));
			sceneDto.setActionList(allActionDto);
			
			List<Dialogue> allDialogues=dialogueDao.findAll();
			List<DialogueDTO> dialogueDtoList = new ArrayList<>();
			for(Dialogue dialogue:allDialogues) {
				ScriptCharector scriptChar=dialogue.getScriptCharector();
				ScriptCharectorDTO scriptCharDto = mapper.map(scriptChar, ScriptCharectorDTO.class);
				
				DialogueDTO dialogueDto = mapper.map(dialogue, DialogueDTO.class);
				dialogueDto.setScDTO(scriptCharDto);

				dialogueDtoList.add(dialogueDto);
				}
			sceneDto.setDialogueList(dialogueDtoList);
			getsceneDtoList.add(sceneDto);
			
		}
		
		
		return getsceneDtoList ;
	}
	


	@Override

	public SceneDTO deleteScene(Long sceneid) throws DataNotFoundException, ApplicationException, Exception {
		log.info("sceneid {}" + sceneid.toString());
		SceneDTO sceneDto = null;
		Optional<Scene> optionalScene = sceneDao.findById(sceneid);
		if (optionalScene.isPresent()) {
			Scene scene = optionalScene.get();
			log.info(scene.toString());
			sceneDto = mapper.map(scene, SceneDTO.class);
			// sceneDao.deleteById(sceneid);

			List<Dialogue> dialogueList = dialogueDao.getDialogueList(sceneid);

			log.info("got dialogueList" + dialogueList);
			dialogueDao.deleteAll(dialogueList);
			log.info("dialouges deleted");
			List<Action> actionList = actionDao.getActions(sceneid);
			log.info("actionList" + actionList);
			actionDao.deleteAll(actionList);
			log.info("actions deleted");

			this.sceneDao.deleteById(sceneid);

		}

		return sceneDto;
	}

	@Override
	public SceneDTO updateScene(SceneDTO sceneDTO) throws DataNotFoundException, ApplicationException, Exception {

		this.deleteScene(sceneDTO.getId());
		sceneDTO.setId(null);
		this.saveScene(sceneDTO);
		return sceneDTO;
	}



	

	public void getexcelinScenes(HttpServletResponse response) throws Exception {
	    Workbook workBook = new HSSFWorkbook();
	    HSSFSheet workSheet = (HSSFSheet) workBook.createSheet("Scene");
	    HSSFSheet locationSheet = (HSSFSheet) workBook.createSheet("location");
	    HSSFSheet actionSheet = (HSSFSheet) workBook.createSheet("actions");
	    HSSFSheet dialogueSheet = (HSSFSheet) workBook.createSheet("dialogues");
	    HSSFSheet scriptCharSheet = (HSSFSheet) workBook.createSheet("scriptCharacter");

	    // Create headers for each sheet
	    createSceneHeaders(workSheet);
	    createLocationHeaders(locationSheet);
	    createActionHeaders(actionSheet);
	    createDialogueHeaders(dialogueSheet);
	    createScriptCharHeaders(scriptCharSheet);

	    List<SceneDTO> listOfSceneDto = getAllScenes();

	    int dataIndex = 1;
	    for (SceneDTO sceneDto : listOfSceneDto) {
	        HSSFRow dataRow1 = workSheet.createRow(dataIndex);
	        HSSFRow dataRowLocation = locationSheet.createRow(dataIndex);
//	        HSSFRow dataRowAction = actionSheet.createRow(dataIndex);
//	        HSSFRow dataRowDialogue = dialogueSheet.createRow(dataIndex);
//	        HSSFRow dataRowScriptChar = scriptCharSheet.createRow(dataIndex);

	        
	        dataRow1.createCell(0).setCellValue(sceneDto.getId());
	        dataRow1.createCell(1).setCellValue(sceneDto.getLocationDto().getName());
	        dataRow1.createCell(2).setCellValue(sceneDto.getTimeId());

	        
	        LocationDTO locDto = sceneDto.getLocationDto();
	        dataRowLocation.createCell(0).setCellValue(locDto.getId());
	        dataRowLocation.createCell(1).setCellValue(locDto.getName());
	        dataRowLocation.createCell(2).setCellValue(locDto.getTypeId());

	       
	        int actionRowIndex = 1;
	        for (ActionDTO act : sceneDto.getActionList()) {
	            HSSFRow dataRowActionItem = actionSheet.createRow(actionRowIndex);
	            dataRowActionItem.createCell(0).setCellValue(act.getId());
	            dataRowActionItem.createCell(1).setCellValue(act.getSceneElementOrderId());
	            dataRowActionItem.createCell(2).setCellValue(act.getSceneId());
	            dataRowActionItem.createCell(3).setCellValue(act.getDescription());
	            actionRowIndex++;
	        }

	        
	        int dialogueRowIndex = 1;
	        for (DialogueDTO diaDto : sceneDto.getDialogueList()) {
	            HSSFRow dataRowDialogueItem = dialogueSheet.createRow(dialogueRowIndex);
	            dataRowDialogueItem.createCell(0).setCellValue(diaDto.getId());
	            dataRowDialogueItem.createCell(1).setCellValue(diaDto.getParantical());
	            dataRowDialogueItem.createCell(2).setCellValue(diaDto.getContent());
	            dataRowDialogueItem.createCell(3).setCellValue(diaDto.getSceneElementOrderId());
	            dataRowDialogueItem.createCell(4).setCellValue(diaDto.getSceneID());
	            dialogueRowIndex++;
	        }
	        int scriptCharRowIndex=1;
	        for (DialogueDTO diaDto : sceneDto.getDialogueList()) {
	            ScriptCharectorDTO scChar =diaDto.getScDTO();
	                HSSFRow dataRowScriptCharItem = scriptCharSheet.createRow(scriptCharRowIndex);
	                dataRowScriptCharItem.createCell(0).setCellValue(scChar.getId());
	                dataRowScriptCharItem.createCell(1).setCellValue(scChar.getName());
	                dataRowScriptCharItem.createCell(2).setCellValue(scChar.getScriptId());
	                scriptCharRowIndex++;
	            }

	        dataIndex++;
	        }

	        
	        ServletOutputStream output=response.getOutputStream();
			workBook.write(output);
		workBook.close();
	    }


	
	public void createSceneHeaders(HSSFSheet sheet) {
	    HSSFRow headerRow = sheet.createRow(0);

	    HSSFCell cell1 = headerRow.createCell(0);
	    cell1.setCellValue("Scene ID");

	    HSSFCell cell2 = headerRow.createCell(1);
	    cell2.setCellValue("Location");

	    HSSFCell cell3 = headerRow.createCell(2);
	    cell3.setCellValue("Time ID");
	}

	public void createLocationHeaders(HSSFSheet sheet) {
	    HSSFRow headerRow = sheet.createRow(0);

	    HSSFCell cell1 = headerRow.createCell(0);
	    cell1.setCellValue("Location ID");

	    HSSFCell cell2 = headerRow.createCell(1);
	    cell2.setCellValue("Name");

	    HSSFCell cell3 = headerRow.createCell(2);
	    cell3.setCellValue("Type ID");
	}

	public void createActionHeaders(HSSFSheet sheet) {
	    HSSFRow headerRow = sheet.createRow(0);

	    HSSFCell cell1 = headerRow.createCell(0);
	    cell1.setCellValue("Action ID");

	    HSSFCell cell2 = headerRow.createCell(1);
	    cell2.setCellValue("Scene Element Order ID");

	    HSSFCell cell3 = headerRow.createCell(2);
	    cell3.setCellValue("Scene ID");

	    HSSFCell cell4 = headerRow.createCell(3);
	    cell4.setCellValue("Description");
	}

	public void createDialogueHeaders(HSSFSheet sheet) {
	    HSSFRow headerRow = sheet.createRow(0);

	    HSSFCell cell1 = headerRow.createCell(0);
	    cell1.setCellValue("Dialogue ID");

	    HSSFCell cell2 = headerRow.createCell(1);
	    cell2.setCellValue("Parenthetical");

	    HSSFCell cell3 = headerRow.createCell(2);
	    cell3.setCellValue("Content");

	    HSSFCell cell4 = headerRow.createCell(3);
	    cell4.setCellValue("Scene Element Order ID");

	    HSSFCell cell5 = headerRow.createCell(4);
	    cell5.setCellValue("Scene ID");
	}

	public void createScriptCharHeaders(HSSFSheet sheet) {
	    HSSFRow headerRow = sheet.createRow(0);

	    HSSFCell cell1 = headerRow.createCell(0);
	    cell1.setCellValue("Script Character ID");

	    HSSFCell cell2 = headerRow.createCell(1);
	    cell2.setCellValue("Name");

	    HSSFCell cell3 = headerRow.createCell(2);
	    cell3.setCellValue("Script ID");
	}
	


}
