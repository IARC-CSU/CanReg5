## load Rcan_core
  source(paste(sep="/", script.basename, "Rcan_core.r"))
  

## install packages missing and require them


  ## get Args from canreg  
  skin <- FALSE
  landscape <- FALSE
  logr <- FALSE
  multi_graph <- FALSE
  
  for (i in 1:length(Args)) {
    
    temp <- Args[i]
    pos <- regexpr("=",temp)[1]
    
    if (pos < 0) {
      assign(substring(temp,2), TRUE)
    } 
    else {
      varname <- substring(temp,2,pos-1)
      assign(varname,substring(temp, pos+1)) 
      if (suppressWarnings(!is.na(as.numeric(get(varname))))){
        assign(varname, as.numeric(get(varname)))
      }
    }
  }
  
tryCatch({
  

  canreg_load_packages(c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","bmp", "jpeg"))

  	#create filename from out and avoid double extension (.pdf.pdf)
	if (substr(out,nchar(out)-nchar(ft),nchar(out)) == paste0(".", ft)) {
	  filename <- out
	  out <- substr(out,1,nchar(out)-nchar(ft)-1)
	} else {
	  filename <- paste(out, ft, sep = "." )
	}
  
   # Merge inc and pop
	dt_all <- csu_merge_inc_pop(
		inc_file =inc,
		pop_file =pop,
		var_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
		column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
		)
	

  },

  error = function(e){
    canreg_error_log(e,filename,out,Args,inc,pop)
    quit()
  }
)
  