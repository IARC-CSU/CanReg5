  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
    ## to get canreg argument list
  Args <- commandArgs(TRUE)
  
  ls_args <- list(filename = NULL, out = NULL, inc = NULL, pop = NULL, ft = "")
  tryCatch({
    
    ## source function to check if update needed
    source(paste(sep="/", script.basename, "r-sources", "Rcan_core.r"))

    
    ## load other function 
    source(paste(sep="/", script.basename, "r-sources", "canreg_core.r"))
    source(paste(sep="/", script.basename, "r-sources", "canreg_table.r"))

    # init argument from canreg
    ls_args <- canreg_args(Args)
    
    canreg_table_child_table(ls_args)
  	
  	
    },
  
  error = function(e){
    if (exists("doc")) {
      if (!is.null(ls_args$filename)) try(print(doc, ls_args$filename), silent = TRUE)
     if (!is.null(ls_args$filename) && file.exists(ls_args$filename)) file.remove(ls_args$filename)
    }
    
    canreg_error_log(e,ls_args$filename,ls_args$out,Args,ls_args$inc,ls_args$pop)
  }
)
