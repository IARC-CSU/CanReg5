## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
  Args <- commandArgs(TRUE)
  
  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  ################

  ## install packages missing and require them
  invisible(canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo")))
	
  
  ## get Args from canreg
  fileInc <- canreg_getArgs(Args, "-inc")
  filePop <- canreg_getArgs(Args, "-pop")
  out <- canreg_getArgs(Args, "-out")
  fileType <- canreg_getArgs(Args, "-ft")
  canreg_header <- canreg_getArgs(Args, "-header")
  landscape <- canreg_getArgs(Args, "-landscape", TRUE)
  skin <- canreg_getArgs(Args, "-skin", TRUE)


  
  ## Merge inc and pop
  dt_all <- csu_merge_inc_pop(
    inc_file =fileInc,
    pop_file =filePop,
    var_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX"), 
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )

  #Prepare canreg data for count per sex and age group
  dt_all <- canreg_age_cases_data(dt_all, skin=skin)
  

	#create filename from out and avoid double extension (.pdf.pdf)
	if (substr(out,nchar(out)-nchar(fileType),nchar(out)) == paste0(".", fileType)) {
	  filename <- out
	  out <- substr(out,1,nchar(out)-nchar(fileType)-1)
	} else {
	  filename <- paste(out, fileType, sep = "." )
	}
	
  #update header
  if (!skin) {
    canreg_header = paste0(canreg_header, "\n\nAll cancers")
  } else {
    canreg_header = paste0(canreg_header, "\n\nAll cancers but C44")
  }
  
  ##Produce output
  canreg_output(output_type = fileType, filename = out,landscape = landscape,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_all,
                canreg_header = canreg_header)

	#talk to canreg
  cat(paste("-outFile",filename,sep=":"))
	
	
