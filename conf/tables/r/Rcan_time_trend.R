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

  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
  first_age <- as.numeric(substr(agegroup,1,regexpr("-", agegroup)[1]-1))
  last_age <- as.numeric(substr(agegroup,regexpr("-", agegroup)[1]+1,nchar(agegroup)))
  
  ## get age group label
  
  canreg_age_group <- canreg_get_agegroup_label(dt, first_age, last_age)
  
  ##calcul of ASR
  dt<- csu_asr_core(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    var_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_all),
                    first_age = first_age+1,
                    last_age= last_age+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  
  #produce graph
  canreg_output(output_type = ft, filename = out,landscape = FALSE,list_graph = TRUE,
                FUN=canreg_asr_trend_top,
                dt=dt,number = number,
                canreg_header = header,
                ytitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group))
  
  if (ft %in% c("png", "tiff", "svg")) {
    temp_file <- substr(filename,0,nchar(filename)-nchar(ft)-1)
    file.rename(paste0(temp_file,"001.",ft),paste0(temp_file,"-male.",ft))
    file.rename(paste0(temp_file,"002.",ft),paste0(temp_file,"-female.",ft))
    
    cat(paste("-outFile",paste0(temp_file,"-male.",ft),sep=":"))
    cat("\n")
    cat(paste("-outFile",paste0(temp_file,"-female.",ft),sep=":"))
    
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
	  if (file.exists(paste0(temp_file,"002.",ft))) file.remove(paste0(temp_file,"002.",ft))
    }
    
    canreg_error_log(e,filename,out,Args,inc,pop)
  }
)
	
	