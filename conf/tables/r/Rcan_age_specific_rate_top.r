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

  ##Prepare canreg data for ageSpecific rate
	dt_all <- canreg_ageSpecific_rate_data(dt_all)


	
	##Produce output
	canreg_output(output_type = ft, filename = out,landscape = FALSE,list_graph = TRUE,
	              FUN=canreg_ageSpecific_rate_top,
	              dt=dt_all,log_scale = logr,nb_top = number,
				  canreg_header = header)

	#talk to canreg
	
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
	
	
