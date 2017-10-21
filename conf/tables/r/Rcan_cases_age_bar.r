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

  #Prepare canreg data for count per sex and age group
  dt_all <- canreg_age_cases_data(dt_all, skin=skin)
  
	##Produce output
  canreg_output(output_type = ft, filename = out,landscape = landscape,list_graph = FALSE,
                FUN=canreg_cases_age_bar,
                df_data=dt_all,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                canreg_header = header,
                skin=skin)

	#talk to canreg
  cat(paste("-outFile",filename,sep=":"))
	
	
  },
  
  error = function(e){
    if (dev.cur() > 1) {
      dev.off()
      if (file.exists(filename)) file.remove(filename)
    }
    
    canreg_error_log(e,filename,out,Args,inc,pop)
  }
)