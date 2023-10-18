package com.slokam.scriptone.service.impl;


import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.*;
import com.slokam.scriptone.dao.IActionDAO;
import com.slokam.scriptone.dao.IDialogueDAO;
import com.slokam.scriptone.dao.ISceneDAO;
import com.slokam.scriptone.dao.IScriptDAO;
import com.slokam.scriptone.dto.ActionDTO;
import com.slokam.scriptone.dto.DialogueDTO;
import com.slokam.scriptone.dto.LocationDTO;
import com.slokam.scriptone.dto.SceneDTO;
import com.slokam.scriptone.dto.ScriptCharectorDTO;
import com.slokam.scriptone.dto.ScriptDTO;
import com.slokam.scriptone.entity.Action;
import com.slokam.scriptone.entity.Dialogue;
import com.slokam.scriptone.entity.Location;
import com.slokam.scriptone.entity.Scene;
import com.slokam.scriptone.entity.Script;
import com.slokam.scriptone.entity.ScriptCharector;
import com.slokam.scriptone.exception.ApplicationException;
import com.slokam.scriptone.exception.DataNotFoundException;
import com.slokam.scriptone.exception.UserInputException;
import com.slokam.scriptone.service.IScriptService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScriptServiceImpl implements IScriptService {

	@Autowired
	private IScriptDAO scriptdao;
	@Autowired
	private ISceneDAO sceneDao;
    @Autowired
	private IDialogueDAO dialogueDao;
    @Autowired
    private IActionDAO actionDao;
	
	@Autowired
	private ModelMapper modelmapper;

	@Override
	public ScriptDTO savescript(ScriptDTO scriptdTO) throws  Exception {

		if(scriptdTO == null || scriptdTO.getName() == null || scriptdTO.getName().trim().length()==0 )
			
			throw new UserInputException("There is no data found in scriptdto");

	
		Script script = modelmapper.map(scriptdTO, Script.class);
		
		if(script == null )
			throw new ApplicationException("There is no data found in script");
		
		scriptdao.save(script);
		
		scriptdTO.setId(script.getId());
		return scriptdTO;
	}

	@Override
	public ScriptDTO getbyId(Long id) {
		
	Optional<Script> scriptopt =	scriptdao.findById(id);
	ScriptDTO scriptdto = null;
	if(scriptopt.isPresent())
	{
		Script script = scriptopt.get();
		scriptdto =	modelmapper.map(script, ScriptDTO.class);
		
	}
		return scriptdto;
	}

	@Override
	public List<ScriptDTO> getall() {
		
		List<Script> listScript = scriptdao.findAll();
		List<ScriptDTO> listScriptDto = new ArrayList<>();
		
		for(Script script : listScript)
		{
		 ScriptDTO scriptdto =	modelmapper.map(script, ScriptDTO.class);
		 listScriptDto.add(scriptdto);
			
		}
		return listScriptDto;
	}

	@Override
	public List<SceneDTO> getListScenes(Long scriptId) throws DataNotFoundException,ApplicationException,Exception {
		log.info("method started");
		List<Scene> sceneList= sceneDao.getScenes(scriptId);
		log.info("list of scenes "+sceneList);
		List<SceneDTO> sceneDtoList=new ArrayList();
		for(Scene scene:sceneList) {
			Location location= scene.getLocation();
			LocationDTO locationDto= modelmapper.map(location,LocationDTO.class);
			SceneDTO sceneDto= modelmapper.map(scene, SceneDTO.class);
			sceneDto.setLocationDto(locationDto);
			Long sceneId= sceneDto.getId();
			log.info("scene id "+sceneId);
			sceneDtoList.add(sceneDto);
		}
		
		return sceneDtoList;
	}

	@Override
	public void exportToPdf(HttpServletResponse response) throws Exception {
		Document document=new Document(PageSize.A4);
		PdfWriter.getInstance(document, response.getOutputStream());
		
		document.open();
		Font font=FontFactory.getFont(FontFactory.HELVETICA_BOLD);
		font.setSize(19);
		font.setColor(Color.BLUE);
		
		 Paragraph p = new Paragraph("List of scripts", font);
	        p.setAlignment(Paragraph.ALIGN_CENTER);
	         
	        document.add(p);
	         
	        PdfPTable table = new PdfPTable(2);
	        table.setWidthPercentage(100f);
	        table.setWidths(new float[] {1.5f, 1.9f});
	        table.setSpacingBefore(10);
	         
	        tableHeaders(table);
	        writeTableData(table);
	         
	        document.add(table);
	         
	        document.close();
		
		
	}
	
	private void tableHeaders(PdfPTable table) {
		PdfPCell pdfCell=new PdfPCell();
	    pdfCell.setBackgroundColor(Color.BLUE);
		pdfCell.setPadding(5);
		
		Font font=FontFactory.getFont(FontFactory.COURIER);
		font.setColor(Color.WHITE);
		
		pdfCell.setPhrase(new Phrase("Script_id",font));
		table.addCell(pdfCell);
		
		pdfCell.setPhrase(new Phrase("Script_Name",font));
		table.addCell(pdfCell);
		
	}
	
	private void writeTableData(PdfPTable table) {
		List<ScriptDTO> scriptList=getall();
		for(ScriptDTO scriptDto: scriptList) {
			table.addCell(String.valueOf(scriptDto.getId()));
			table.addCell(String.valueOf(scriptDto.getName()));
		}
	}
	
	
	
	
	
	
	
}
