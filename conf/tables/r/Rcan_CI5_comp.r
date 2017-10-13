## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
  Args <- commandArgs(TRUE)
  
  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  source(paste(sep="/", script.basename, "Rcan_source.r"))
  ################

tryCatch({
  
  region_admit <- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  
  if (!sr %in% region_admit) {
    stop(paste0("the actual registry region (", sr, ") is not in the CI5 region defined list.\nPlease edit the region in Canreg (tools -> Databse structure) before running this table."))
  }
  
	##Prepare canreg data for ageSpecific rate
  dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
  
  # import CI5 data with same cancer code and same age group
  dt_CI5_data <- canreg_import_CI5_data(dt_all, paste0(script.basename, "/CI5_data.rds"))
  
  #merge CI5 and canreg data
  dt_both <- canreg_merge_CI5_registry(dt_all,dt_CI5_data, registry_region = sr, registry_label = header, number=number )
  
  #create bar chart color graphique 
  setkeyv(dt_both, c("CSU_RANK", "SEX","asr"))
  dt_both[country_label!=header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
  dt_both[country_label==header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
  
  
  canreg_output(output_type = ft, filename = out,landscape = landscape,list_graph = TRUE,
                FUN=canreg_bar_CI5_compare,
                dt=dt_both,xtitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=",")),
                number=number,text_size_factor=1,multi_graph=TRUE)
  
	
	
	#talk to canreg
  if (ft %in% c("png", "tiff", "svg")) {
    temp_file <- substr(filename,0,nchar(filename)-nchar(ft)-1)
    cat(paste("-outFile",paste0(temp_file,"001.",ft),sep=":"))
    
  } else {
    
    cat(paste("-outFile",filename,sep=":"))
    
  }
	
	
  },
  
  error = function(e){
    if (dev.cur() > 1) {
      dev.off()
      temp_file <- substr(filename,0,nchar(filename)-nchar(ft)-1)
      if (file.exists(filename)) file.remove(filename)
      if (file.exists(paste0(temp_file,"001.",ft))) file.remove(paste0(temp_file,"001.",ft))
    }
    
    canreg_error_log(e,filename,out,Args,inc,pop)
  }
)
	
	
