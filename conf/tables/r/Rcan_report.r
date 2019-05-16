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
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png","shiny.i18n", "Rcan"))
	i18n <- Translator(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
	i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )

  dt_basis <- csu_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )


  graph_width <- 6

  #check if report path exist (if not create report path)
  pos <- regexpr("\\\\[^\\\\]*$", ls_args$out)[[1]]
  path <- substr(ls_args$out,start=1, stop=pos)
  if (ls_args$sc=="null") {
    report_path <- paste0(path, "report-template")
  } else {
    report_path <- paste0(path, "report-template-", ls_args$sc)
  }
  if(!file_test("-d",report_path)) {
    dir.create(report_path)
  }

  
  sysName <- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <- winProgressBar(
      title = "Create docx",
      label = "Initializing"
    )
  } 

  
  doc <- read_docx(paste(sep="/", script.basename,"slide_template", "template.docx"))
  
  
  doc <- rcan_report(doc, report_path, dt_all, ls_args)
  
  print(doc, ls_args$filename)

  if (sysName == "Windows") {
    close(pb)
  }
  
  #reporteRs_OO_patched(docx=ls_args$filename)
  

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
