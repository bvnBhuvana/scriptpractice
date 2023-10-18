package com.slokam.scriptone.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slokam.scriptone.dao.IlocationTypeDao;
import com.slokam.scriptone.dto.LocationTypeDto;
import com.slokam.scriptone.dto.ScriptDTO;
import com.slokam.scriptone.entity.LocationType;
import com.slokam.scriptone.entity.Script;
import com.slokam.scriptone.service.IlocationService;


import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class LocationTypeServiceImpl implements IlocationService {
	
	@Autowired
	private IlocationTypeDao iLocationDao;
	
	@Autowired
	private ModelMapper modelmapper;

	@Override
	public LocationTypeDto save(LocationTypeDto loctypedto) {
		   log.info("loactionType save method Started");  
		   LocationType locationtype =  modelmapper.map(loctypedto, LocationType.class);       
		   iLocationDao.save(locationtype);
		   loctypedto.setId(locationtype.getId());
		   
		return loctypedto;
	}

	@Override
	public LocationTypeDto getbyid(Integer id) {
	
		LocationTypeDto loctypedto = null;
		   Optional<LocationType> loctype =  iLocationDao.findById(id);
		    if(loctype.isPresent())
		     {
		        LocationType loc =  loctype.get();
		  	 loctypedto =  	modelmapper.map(loc, LocationTypeDto.class);
		     }
		return loctypedto;
	}

	@Override
	public List<LocationTypeDto> getbyall() {
		 List<LocationTypeDto> locationtypedto = new ArrayList<>();
		 LocationTypeDto locationdto = null;
	     List<LocationType>  loclist =   iLocationDao.findAll();
	   
	     for(LocationType loc : loclist)
	     {
	    	 locationdto =  modelmapper.map(loc, LocationTypeDto.class);
	    	 locationtypedto.add(locationdto);
	    	
	     }
		return locationtypedto;
	}

	@Override
	public LocationTypeDto deletebyid(Integer id) {
		     LocationTypeDto loctypedto =  getbyid(id);
		     iLocationDao.deleteById(id);
		return loctypedto;
	}

	@Override
	public void getLocationTypeinExcel(HttpServletResponse response) {
		
		try(Workbook workBook =new HSSFWorkbook()){
			HSSFSheet workSheet= (HSSFSheet) workBook.createSheet("locationType");
			HSSFRow row=workSheet.createRow(0);
			
		//	List<LocationType> locationTypeList=iLocationDao.findAll();
			List<LocationTypeDto> locationdtos=getbyall();
			
			row.createCell(0).setCellValue("id");
			row.createCell(1).setCellValue("description");
			
			
			int dataRowIndex=1;
			
			for(LocationTypeDto locationTypeDto:locationdtos) {
				HSSFRow dataRow=workSheet.createRow(dataRowIndex);
				dataRow.createCell(0).setCellValue(locationTypeDto.getId());
				dataRow.createCell(1).setCellValue(locationTypeDto.getDescription());
				
				
				dataRowIndex++;
				
			}
			
			ServletOutputStream outputstreem=  response.getOutputStream();
			workBook.write(outputstreem);
			
		}catch(IOException e) {
			System.out.println(e);
		}
		
		
	}
	
	
	                   	
	
	

}
