package com.slokam.scriptone.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slokam.scriptone.dto.LocationTypeDto;
import com.slokam.scriptone.service.IlocationService;



@RestController
@RequestMapping("locationtype")
public class LocationTypeController {
	
	@Autowired
	private IlocationService ilocservice;
	
	@PostMapping("savelocationtype")
	public ResponseEntity<LocationTypeDto> savelocationtype(@RequestBody LocationTypeDto locdto)
	{
		       LocationTypeDto loctypedto = ilocservice.save(locdto); 
		        ResponseEntity<LocationTypeDto> re;
		        re = new ResponseEntity<LocationTypeDto>(loctypedto,HttpStatus.OK);
		        return re;
	}
	
	
	
	@GetMapping("/{id}")
	public ResponseEntity<LocationTypeDto> findbylocationid(@PathVariable Integer id)
	{
		             LocationTypeDto loc =  ilocservice.getbyid(id);
		       
		       return ResponseEntity.status(HttpStatus.OK).body(loc);
	}
	
	@DeleteMapping("deletebyid/{id}")
	public ResponseEntity<LocationTypeDto> deletebyid(@PathVariable Integer id)
	{
		             LocationTypeDto locdtp = ilocservice.deletebyid(id);
		    return ResponseEntity.status(HttpStatus.OK).body(locdtp);
	}
	
	@GetMapping("getalllocationtype")
	public ResponseEntity<List<LocationTypeDto>> getalllocationtype()
	{
		              List<LocationTypeDto> locationdto = ilocservice.getbyall();
		      return ResponseEntity.status(HttpStatus.OK).body(locationdto);
		
	}

	@GetMapping("/locationtypesexcel")
	public void getLocationTypesExcel(HttpServletResponse response) {
		response.setContentType("application/octet-stream");
		String headerKey="Content-Disposition";
		String headerValue="attachment;filename=locationtype.xls";
		response.setHeader(headerKey, headerValue);
		
		ilocservice.getLocationTypeinExcel(response);
		
		
		
		
	}
}
