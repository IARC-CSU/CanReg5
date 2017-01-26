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
  invisible(canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales")))
	
  
  ## get Args from canreg
  fileInc <- canreg_getArgs(Args, "-inc")
  filePop <- canreg_getArgs(Args, "-pop")
  out <- canreg_getArgs(Args, "-out")
  fileType <- canreg_getArgs(Args, "-ft")
  log_scale <-canreg_getArgs(Args, "-logr", boolean = TRUE)
  multi_graph <-canreg_getArgs(Args, "-multi_graph", boolean = TRUE)
  canreg_header <- canreg_getArgs(Args, "-header")

  
	## Merge inc and pop
	dt_all <- csu_merge_inc_pop(
		inc_file =fileInc,
		pop_file =filePop,
		var_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX"), 
		column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
		)
    
  ##Prepare canreg data for ageSpecific rate
	dt_all <- canreg_ageSpecific_rate_data(dt_all)
	
	#create filename from out and avoid double extension (.pdf.pdf)
	if (substr(out,nchar(out)-nchar(fileType),nchar(out)) == paste0(".", fileType)) {
	  filename <- out
	  out <- substr(out,1,nchar(out)-nchar(fileType)-1)
	} else {
	  filename <- paste(out, fileType, sep = "." )
	}
	
	##Produce output
	canreg_output(output_type = fileType, filename = out,landscape = FALSE,
	              list_graph = TRUE,
	              FUN=canreg_ageSpecific_rate_multi_plot,dt=dt_all,var_by="SEX",var_age_label_list = "AGE_LABEL",
	              log_scale = log_scale,  
	              color_trend=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
	              multi_graph= multi_graph,
				  canreg_header=canreg_header)
	
	
	##talk to canreg
	cat(paste("-outFile",filename,sep=":"))
	
	
