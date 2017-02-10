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
	nb_top <- as.numeric(canreg_getArgs(Args, "-number"))
	landscape <- canreg_getArgs(Args, "-landscape", TRUE)
	canreg_header <- canreg_getArgs(Args, "-header")
	age_group <- (canreg_getArgs(Args, "-agegroup"))
  
	## Merge inc and pop
	dt_all <- csu_merge_inc_pop(
		inc_file =fileInc,
		pop_file =filePop,
		var_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX"), 
		column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
		)
    
	##Prepare canreg data for ageSpecific rate
	dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
	
	#parse agegroup and get age group label
	first_age <- as.numeric(substr(age_group,1,regexpr("-", age_group)[1]-1))
	last_age <- as.numeric(substr(age_group,regexpr("-", age_group)[1]+1,nchar(age_group)))
	temp_max <- max(dt_all$AGE_GROUP)
	temp_min <- min(dt_all$AGE_GROUP)
	if (temp_max < last_age) {
	  last_age = temp_max
	} 
	if (temp_min > first_age) {
	  temp_min = first_age
	} 
	temp1 <- as.character(unique(dt_all[dt_all$AGE_GROUP == first_age,]$AGE_GROUP_LABEL))
	temp2 <-as.character(unique(dt_all[dt_all$AGE_GROUP == last_age,]$AGE_GROUP_LABEL))
	temp1 <- substr(temp1,1,regexpr("-", temp1)[1]-1)
	temp2 <- substr(temp2,regexpr("-", temp2)[1]+1,nchar(temp2))
	canreg_age_group <- paste0(temp1,"-",temp2, " years")
	
	##calcul of ASR
	dt_all<- csu_asr_core(df_data =dt_all, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
						   var_by = c("cancer_label", "SEX"), missing_age = canreg_missing_age(dt_all),
						   first_age = first_age+1,
						   last_age= last_age+1,
						   pop_base_count = "REFERENCE_COUNT",
						   age_label_list = "AGE_GROUP_LABEL")
	
	#create filename from out and avoid double extension (.pdf.pdf)
	if (substr(out,nchar(out)-nchar(fileType),nchar(out)) == paste0(".", fileType)) {
	  filename <- out
	  out <- substr(out,1,nchar(out)-nchar(fileType)-1)
	} else {
	  filename <- paste(out, fileType, sep = "." )
	}
	
	#produce graph
	canreg_output(output_type = fileType, filename = out,landscape = landscape,list_graph = FALSE,
              FUN=canreg_ASR_bar_top,
              df_data=dt_all,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = nb_top,
			  canreg_header = canreg_header,
              canreg_age_group = canreg_age_group)
	
	
	##talk to canreg
	cat(paste("-outFile",filename,sep=":"))
	
	
