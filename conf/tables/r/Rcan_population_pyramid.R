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

  #Prepare canreg data population pyramid
  dt_all <- canreg_pop_data(dt_all)

	##Produce output
  canreg_output(output_type = ft, filename = out,landscape = landscape,list_graph = FALSE,
                FUN=canreg_population_pyramid,
                df_data=dt_all,
                canreg_header = header)

	#talk to canreg
  cat(paste("-outFile",filename,sep=":"))
	
	
