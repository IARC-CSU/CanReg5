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
                    var_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                    first_age = first_age+1,
                    last_age= last_age+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  ##Keep top based on rank
  dt <- csu_dt_rank(dt,
                    var_value= "CASES",
                    var_rank = "cancer_label",
                    group_by = "SEX",
                    number = number
  )
  
  
  ##calcul eapc
  dt_eapc <- csu_eapc_core(dt, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX","CSU_RANK"))
  dt_eapc <-as.data.table(dt_eapc)
  
  
  ##produce graph
  canreg_output(output_type = ft, filename = out,landscape = landscape,list_graph = FALSE,
                FUN=canreg_eapc_scatter,
                dt_plot=dt_eapc,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                canreg_header = header,
                ytitle=paste0("Estimated Average Percentage Change (%), ", canreg_age_group))

	
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
	
	
