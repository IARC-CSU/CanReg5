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
  canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","bmp", "jpeg"), Rcan_source=script.basename)
  
  #merge incidence and population
  dt_all <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  region_admit <- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  
  if (!ls_args$sr %in% region_admit) {
    stop(paste0("the actual registry region (", ls_args$sr, ") is not in the CI5 region defined list.\nPlease edit the region in Canreg (tools -> Databse structure) before running this table."))
  }
  
	##Prepare canreg data for ageSpecific rate
  dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
  
  # import CI5 data with same cancer code and same age group
  dt_CI5_data <- canreg_import_CI5_data(dt_all, paste0(script.basename, "/CI5_data.rds"))
  
  #merge CI5 and canreg data
  dt_both <- canreg_merge_CI5_registry(dt_all,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header, number=ls_args$number )
  
  #create bar chart color graphique 
  setkeyv(dt_both, c("CSU_RANK", "SEX","asr"))
  dt_both[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
  dt_both[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
  
  
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = TRUE,
                FUN=canreg_bar_CI5_compare,
                dt=dt_both,xtitle=paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=",")),
                number=ls_args$number,text_size_factor=1,multi_graph=TRUE)
  
	
	
	 #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=TRUE)
  
  
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
	
