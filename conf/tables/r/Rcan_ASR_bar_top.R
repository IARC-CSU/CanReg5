## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
  Args <- commandArgs(TRUE)
  
  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  source(paste(sep="/", script.basename, "Rcan_source.r"))
  ################

  
	## Merge inc and pop
	dt_all <- csu_merge_inc_pop(
		inc_file =inc,
		pop_file =pop,
		var_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX"), 
		column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
		)
    
	##Prepare canreg data for ageSpecific rate
	dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)

	first_age <- as.numeric(substr(agegroup,1,regexpr("-", agegroup)[1]-1))
	last_age <- as.numeric(substr(agegroup,regexpr("-", agegroup)[1]+1,nchar(agegroup)))
	
	## get age group label
	canreg_age_group <- canreg_get_agegroup_label(dt_all, first_age, last_age)
	
	##calcul of ASR
	dt_all<- csu_asr_core(df_data =dt_all, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
						   var_by = c("cancer_label", "SEX"), missing_age = canreg_missing_age(dt_all),
						   first_age = first_age+1,
						   last_age= last_age+1,
						   pop_base_count = "REFERENCE_COUNT",
						   age_label_list = "AGE_GROUP_LABEL")
		
	#produce graph
	canreg_output(output_type = ft, filename = out,landscape = landscape,list_graph = FALSE,
              FUN=canreg_ASR_bar_top,
              df_data=dt_all,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = number,
			  canreg_header = header,
              canreg_age_group = canreg_age_group)
	
	
	##talk to canreg
	cat(paste("-outFile",filename,sep=":"))
	
	
