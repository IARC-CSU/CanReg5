  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  
  ## Load Rcan function
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  
  ## to get canreg argument list
  Args <- commandArgs(TRUE)
  ls_args <- canreg_args(Args)
  
  
tryCatch({
  
  #load dependency packages
	canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","bmp", "jpeg", "shiny.i18n", "Rcan"))
	i18n <- Translator(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
	i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  
  year_info <- canreg_get_years(dt_all)
  if (year_info$span < 2) {
    stop(i18n$t("Time trend analysis need at least 2 years data"))
  }

  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
  
  ## get age group label
  
  canreg_age_group <- canreg_get_agegroup_label(dt, ls_args$agegroup)
  
  ##calcul of ASR
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                    first_age = canreg_age_group$first_age+1+1,
                    last_age= canreg_age_group$last_age+1+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  
  #produce graph
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = TRUE,
                FUN=canreg_asr_trend_top,
                dt=dt,number = ls_args$number,
                canreg_header = ls_args$header,
                ytitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
  
  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=TRUE)
  
  
  	},
  
  error = function(e){
    if (dev.cur() > 1) {
      dev.off()
	  temp_file <- substr(ls_args$filename,0,nchar(ls_args$filename)-nchar(ls_args$ft)-1)
      if (file.exists(ls_args$filename)) file.remove(ls_args$filename)
	  if (file.exists(paste0(temp_file,"001.",ls_args$ft))) file.remove(paste0(temp_file,"001.",ls_args$ft))
	  if (file.exists(paste0(temp_file,"002.",ls_args$ft))) file.remove(paste0(temp_file,"002.",ls_args$ft))
    }
    
    canreg_error_log(e,ls_args$filename,ls_args$out,Args,ls_args$inc,ls_args$pop)
  }
)
	
	