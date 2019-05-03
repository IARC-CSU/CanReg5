  ## To get the R folder of the actual script
  initial.options <- commandArgs(trailingOnly = FALSE)
  file.arg.name <- "--file="
  script.name <- sub(file.arg.name, "", 
                     initial.options[grep(file.arg.name, initial.options)])
  script.basename <- dirname(script.name)
  

  
  ## to get canreg argument list
  Args <- commandArgs(TRUE)
  
tryCatch({
  
  shiny_dir <- paste(sep="/", script.basename, "shiny")
  runApp(appDir =shiny_dir, launch.browser =TRUE)
 

#talk to canreg
  cat(paste("-outFile",ls_args$filename,sep=":"))
	
	
  },
  
  error = function(e){
    if (exists("doc")) {
      print(doc, ls_args$filename)
     if (file.exists(ls_args$filename)) file.remove(ls_args$filename)
    }
    
    canreg_error_log(e,ls_args$filename,ls_args$out,Args,ls_args$inc,ls_args$pop)
  }
)
	
	
	
	
