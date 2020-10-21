  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
    ## to get canreg argument list
  Args <- commandArgs(TRUE)

  tryCatch({
    ## source function to check if update needed
    source(paste(sep="/", script.basename, "r-sources", "Rcan_core.r"))

    
    ## load other function 
    source(paste(sep="/", script.basename, "r-sources", "canreg_core.r"))
    source(paste(sep="/", script.basename, "r-sources", "canreg_table.r"))

    # init argument from canreg
    ls_args <- canreg_args(Args)

    canreg_table_cases_age_pie(ls_args)
  
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
	
