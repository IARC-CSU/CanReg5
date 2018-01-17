

CSU_country_info <- function(df_data,var_code, folder_dict) {
  
  df_data <- data.table(df_data)
  
  if (var_code == "country_code") {
    
    df_data[, country_code_old:=NULL]
    setnames(df_data, "country_code", "country_code_old")
  }
  
  setnames(df_data, var_code, "CSU_varcode")
  
  if (mean(df_data[["CSU_varcode"]]) > 100000) {
    
    df_data[CSU_varcode==250091,CSU_varcode:= 474000] #special for french martinique for CI5XI
    df_data[CSU_varcode==250093,CSU_varcode:= 540000] #special for new caledonia for CI5XI
    df_data[CSU_varcode==250094,CSU_varcode:= 254000] #special for french guiana for CI5XI
    
  
    df_data[, temp:=CSU_varcode/1000]
    df_data[, country_code:=floor(temp)]
    df_data[, regional:=ifelse(temp-country_code>0,1,0)]
    
  } else if (mean(df_data[["CSU_varcode"]]) > 1000) {
    
    df_data[, temp:=CSU_varcode/100]
    df_data[, country_code:=floor(temp)]
    df_data[, regional:=ifelse(temp-country_code>0,1,0)]
    
    
  } else {
    
    df_data[, country_code:=CSU_varcode]
    df_data[, regional:=0]
    
  }
  
  setnames(df_data, "CSU_varcode",var_code )
  
  df_data[,temp := NULL]
  df_data[country_code==891, country_code:=688] #special for serbia in CI5X
  
  df_UNcode<- read.csv(paste0(folder_dict, "UN_country_info.csv"))
  df_data <- merge(df_data,df_UNcode, by=c("country_code"),  all.x = TRUE)  
  
  unique(df_data$country_label)
  
  list_country <- unique(df_data[is.na(country_label) & is.na(area_code) & is.na(area_label)]$country_code)
  
  
  if (length(list_country) > 0) {
    cat("these country have not been associated with an area!!\n")
    cat(list_country)
  } else {
    cat("All country have been associated with an area!!")
  }
  
  return(df_data)
  
}




canreg_error_log <- function(e,filename,out,Args,inc,pop) {
  
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
  

  
  #create log error file name 
  log_name <- paste0(gsub("\\W","", label),"_",sc,"_",gsub("\\D","", Sys.time()),"_error_log.txt") 

  #find path and create log file
  pos <- max(gregexpr("\\", out, fixed=TRUE)[[1]])
  path <- substr(out,start=1, stop=pos)
  log_file <- paste0(path, log_name)
  error_connection <- file(log_file,open="wt")
  sink(error_connection)
  sink(error_connection, type="message")
  
  #print error
  cat(paste0("An error occured! please send the log file: `",log_file,"` to canreg@iarc.fr.\n"))
  cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
  print(paste("MY_ERROR:  ",e))
  cat("\n")
  
  #print argument from canreg
  print(Args)
  cat("\n")
  
  #print environment
  print(ls.str())
  cat("\n")
  
  #print R version and package load
  print(sessionInfo())
  cat("\n")
  
  #print java information if windows
  if (Sys.info()[['sysname']] == "Windows") {
    print(find.java())
  }
  
  #print missing package

  packages_list <- c("Rcpp", "data.table", "ggplot2", "gridExtra", "scales", "Cairo","grid","ReporteRs", "zip", "bmp", "jpeg")

  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]  
  if (length(missing_packages) == 0) {
    print("No missing package")
  } else {
    print(missing_packages)
  }
  cat("\n")
  
  #test loading package
  lapply(packages_list, require, character.only = TRUE)
  cat("\n")
  
  print("----------------------")
  cat("\n")
  #print incidence / population file (r format)
  cat("Incidence file\n")
  dput(read.table(inc, header=TRUE))
  cat("\n")
  cat("population file\n")
  dput(read.table(pop, header=TRUE))
  cat("\n")
  
  #close log_file and send to canreg
  sink(type="message")
  sink()
  close(error_connection)
  
  
  
  
  cat(paste("-outFile",log_file,sep=":"))
  
}



canreg_args <- function(Args) {
  
  
  arg_list <- list()
  
  ## get Args from canreg  
  
  for (logi_name in c("skin","landscape","logr","multi_graph")) {
    
    arg_list[[logi_name]] <- FALSE
  }
  
  
  for (i in 1:length(Args)) {
    
    temp <- Args[i]
    pos <- regexpr("=",temp)[1]
    
    if (pos < 0) {
      arg_list[[substring(temp,2)]] <- TRUE
    } 
    else {
      varname <- substring(temp,2,pos-1)
      assign(varname,substring(temp, pos+1)) 
      if (suppressWarnings(!is.na(as.numeric(get(varname))))){
        assign(varname, as.numeric(get(varname)))
      }
    }
    arg_list[[varname]] <- get(varname)
  }
  
  
  if (substr(arg_list$out,nchar(arg_list$out)-nchar(arg_list$ft),nchar(arg_list$out)) == paste0(".", arg_list$ft)) {
    arg_list[["filename"]] <- arg_list$out
    arg_list$out <- substr(arg_list$out,1,nchar(arg_list$out)-nchar(arg_list$ft)-1)
  } else {
    arg_list[["filename"]] <- paste(arg_list$out, arg_list$ft, sep = "." )
  }
  
  
  return(list=arg_list)
  
  
}



canreg_load_packages <- function(packages_list, Rcan_source=NULL) { 
  
  
  if (getRversion() == '3.2.0') {
   
    stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
    
  }
  
  dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
  .libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))
  
  list_installed_packages <- installed.packages()[,"Package"]
  
  missing_packages <- packages_list[!(packages_list %in% list_installed_packages)]
  
  #managing installing package for old R version. 
  if (getRversion() < '3.2.0') {
    utils::setInternet2(TRUE)
    if (Sys.info()[['sysname']] == "Windows") {
      options(download.file.method = "internal")
    } else if (Sys.info()[['sysname']] == "Linux") {
      options(download.file.method = "wget")
    } else if (Sys.info()[['sysname']] == "Darwin") {
      options(download.file.method = "curl")
    }
  } else if (getRversion() < '3.3.0') {
    if (Sys.info()[['sysname']] == "Windows") {
      options(download.file.method = "wininet")
    } else {
      options(download.file.method = "libcurl")
    }
  }
  
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 

  new.repos["CRAN"] <- "https://cran.r-project.org" #set your favorite  CRAN Mirror here 

  options(repos = new.repos) 
  
  if (!"Rcpp" %in% missing_packages) {
    if (packageVersion("Rcpp") < "0.11.0") {
      missing_packages <- c(missing_packages,"Rcpp" )
    }
  }
  
  if (!"ggplot2" %in% missing_packages) {
    if (packageVersion("ggplot2") < "2.2.0") {
      missing_packages <- c(missing_packages,"ggplot2" )
    }
  }
  
  if (!"data.table" %in% missing_packages) {
    if (packageVersion("data.table") < "1.9.6") {
      missing_packages <- c(missing_packages,"data.table" )
    }
  }
  
  if (!"scales" %in% missing_packages) {
    if (packageVersion("scales") < "0.4.1") {
      missing_packages <- c(missing_packages,"scales" )
    }
  }
  
  if ("scales" %in% missing_packages) {
    
    if ("munsell" %in% list_installed_packages) {
      if (packageVersion("munsell") < "0.2") {
        missing_packages <- c(missing_packages,"munsell" )
      }
    }
  }

  if ("ggplot2" %in% missing_packages) {
    
    if ("gtable" %in% list_installed_packages) {
      if (packageVersion("gtable") < "0.1.1") {
        missing_packages <- c(missing_packages,"gtable" )
      }
    }
    if ("plyr" %in% list_installed_packages) {
      if (packageVersion("plyr") < "1.7.1") {
        missing_packages <- c(missing_packages,"plyr" )
      }
    }
  }

  
  
  if ("ReporteRs" %in% missing_packages) {
     
    if ("rvg" %in% list_installed_packages) {
      if (packageVersion("rvg") < "0.1.2") {
        missing_packages <- c(missing_packages,"rvg")
      }
    }
  }


  missing_packages <- unique(missing_packages)
  


  if(length(missing_packages) > 0 ) {
    
    if (Sys.info()[['sysname']] == "Windows") {
      options(pkgType="win.binary") #to avoid package from source which need compilation.
    }
    
    for (i in missing_packages) {
      install.packages(i, dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)

    }
  }
  
  
  #install Rcan package
  
  Rcan_file <- list.files(path=Rcan_source, pattern= "Rcan_\\d\\.\\d\\.\\d\\.tar\\.gz")
  Rcan_version <- regmatches(Rcan_file,regexpr(pattern= "\\d\\.\\d\\.\\d", Rcan_file))

  
  if ("Rcan" %in% list_installed_packages) {
    if (packageVersion("Rcan") < Rcan_version) {
      install.packages(paste0(Rcan_source, "/",Rcan_file), repos=NULL)
    }
  } else {
      install.packages(paste0(Rcan_source, "/",Rcan_file), repos=NULL)
  }
      

  lapply(packages_list, require, character.only = TRUE)
  library(Rcan)
  
}

canreg_output_cat <- function(ft, filename,sex_graph=FALSE, list_graph=FALSE) {
  
  if (ft %in% c("png", "tiff", "svg") & sex_graph ) {
    

    temp_file <- substr(filename,0,nchar(filename)-nchar(ft)-1)
    file.rename(paste0(temp_file,"001.",ft),paste0(temp_file,"-male.",ft))
    file.rename(paste0(temp_file,"002.",ft),paste0(temp_file,"-female.",ft))
    
    cat(paste("-outFile",paste0(temp_file,"-male.",ft),sep=":"))
    cat("\n")
    cat(paste("-outFile",paste0(temp_file,"-female.",ft),sep=":"))
    
  } else if (ft %in% c("png", "tiff", "svg") & list_graph ) {
      
    temp_file <- substr(filename,0,nchar(filename)-nchar(ft)-1)  
    cat(paste("-outFile",paste0(temp_file,"001.",ft),sep=":"))
      
  } else {
    
    cat(paste("-outFile",filename,sep=":"))
    
  }
  
}


canreg_missing_age <- function(dt,
                               var_age = "AGE_GROUP",
                               var_age_label = "AGE_GROUP_LABEL") {

  
  if (NA %in% dt[[var_age_label]]) {
    missing_age <- unique(dt[is.na(as.character(get(var_age_label))), c(var_age, var_age_label), with = FALSE])[[var_age]]
  } else {
    missing_age <- 10000
  }
  return(missing_age)
}

canreg_cancer_info <- function(dt,
                               var_cancer_label = "ICD10GROUPLABEL") {
  
  cancer_label <- substring(as.character(dt[[var_cancer_label]]),4,nchar(as.character(dt[[var_cancer_label]])))
  cancer_sex <- substring(as.character(dt[[var_cancer_label]]),1,3)
  
  return(list(cancer_label = cancer_label, cancer_sex = cancer_sex))
  
}


canreg_get_years <- function (dt, var_year="YEAR") {
  
  dt <- unique(dt[[var_year]])
  span <- max(dt) - min(dt) + 1
  
  return(list(span = span, min = min(dt), max=max(dt)))
  
} 


canreg_import_CI5_data <- function(dt,CI5_file,var_ICD_canreg="ICD10GROUP",var_age_label_canreg="AGE_GROUP_LABEL") {
  
  # list ICD10 from canreg
  ICD_canreg <- unique(dt[[var_ICD_canreg]])
  ICD_canreg <- ICD_canreg[grepl("^C",ICD_canreg )]
  
  #load CI5 data
  dt_CI5_data <- as.data.table(readRDS(file = CI5_file))
  
  #list ICD10 CI5
  ICD_CI5 <- unique(dt_CI5_data[["ICD10"]])
  ICD_CI5 <- ICD_CI5[grepl("^C",ICD_CI5 )]
  
  #parse ICD10 code 
  dt_ICD_CI5 <- data.table(ICD10=ICD_CI5,ICD_list=(lapply(ICD_CI5,parse_icd10)))
  dt_ICD_canreg <- data.table(ICD10=ICD_canreg,ICD_list=(lapply(ICD_canreg,parse_icd10)))
  dt_ICD_CI5$ICD_canreg <- character(0) 
  
  #associate CI5 ICD code with CANREG ICD code
  for (i in 1:nrow(dt_ICD_CI5)) {
    
    temp <- sapply(dt_ICD_canreg$ICD_list, function(x) {return(all(dt_ICD_CI5$ICD_list[[i]] %in% x))})
    ind <- which(temp)
    if (length(ind) > 0) {
      dt_ICD_CI5$ICD_canreg[[i]] <- as.character(dt_ICD_canreg$ICD10)[ind]
    }
    
  }
  
  
  #Add canreg ICD code to CI5 data
  dt_CI5_data <- merge(dt_CI5_data,dt_ICD_CI5, by=("ICD10"), all.x=TRUE) 
  dt_CI5_data <- dt_CI5_data[!is.na(ICD_canreg),]
  dt_CI5_data<-  dt_CI5_data[,.( cases=sum(cases), py=mean(py)), by=c("sex","age","country_label", "cr","ICD_canreg")]
  
  
  #list age group label from canreg and CI5
  dt_CI5_data[age<19, age_label:= (age-1)*5]
  dt_CI5_age_label <- parse_age_label_dt(dt_CI5_data,var_age_label = "age_label")
  dt_canreg_age_label <- parse_age_label_dt(dt,var_age_label =var_age_label_canreg)
  dt_CI5_age_label$age_label_canreg <- character(0) 
  
  #associate CI5 age group with CANREG age group and age code and reference count
  for (i in 1:nrow(dt_CI5_age_label)) {
    
    temp <- sapply(dt_canreg_age_label$age_list, function(x) {return(all(dt_CI5_age_label$age_list[[i]] %in% x))})
    ind <- which(temp)
    if (length(ind) > 0) {
      dt_CI5_age_label$age_label_canreg[[i]] <- as.character(dt_canreg_age_label$age_label[ind])

    }
  }
  
  dt_CI5_age_label[,age_list:=NULL]

  setnames(dt_CI5_age_label,"age_label_canreg",var_age_label_canreg)
  dt_temp <- unique(dt_all[,c("AGE_GROUP","REFERENCE_COUNT", var_age_label_canreg),  with=FALSE])
  dt_CI5_age_label <- merge(dt_CI5_age_label,dt_temp, by=c(var_age_label_canreg),all.x=TRUE, all.y=FALSE )
  
  
  #Add canreg age label code to CI5 data
  dt_CI5_data <- merge(dt_CI5_data,dt_CI5_age_label, by=("age_label"), all.x=TRUE) 
  dt_CI5_data<-  dt_CI5_data[,.( cases=sum(cases), py=sum(py)), by=c("sex","AGE_GROUP","REFERENCE_COUNT",var_age_label_canreg,"country_label", "cr","ICD_canreg")]
  
  dt_CI5_data[, ICD10GROUP:=factor(ICD_canreg, levels = levels(dt$ICD10GROUP))]
  dt_CI5_data[, SEX:=factor(sex, levels=c(1,2),labels = levels(dt$SEX))]
  dt_CI5_data[,sex:=NULL]
  dt_CI5_data[,ICD_canreg:=NULL]
  
  return(dt_CI5_data)
}


canreg_merge_CI5_registry <- function(dt, dt_CI5, registry_region, registry_label, number=5) {
  
  ##calcul of ASR for canreg
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX","ICD10GROUP","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  ##keep top 5 cancer for men and top 5 cancer women of canreg.
  dt <- Rcan:::core.csu_dt_rank(dt,var_value = "CASES",var_rank = "cancer_label",
                    group_by = "SEX", number =number, ties.method = "first") 
  
  #Keep selected cancer in CI5 data and prepare CI5 data
  
  dt_temp <- dt[,c("SEX", "ICD10GROUP", "CSU_RANK"),  with=FALSE]
  dt_CI5 <- merge(dt_CI5,dt_temp, by=c("SEX", "ICD10GROUP"),all.y=TRUE )
  dt_CI5 <- Rcan:::core.csu_asr(df_data =dt_CI5, var_age ="AGE_GROUP",var_cases = "cases", var_py = "py",
                         group_by = c("country_label","cr" ,"SEX","ICD10GROUP", "CSU_RANK"),
                         var_age_group=c("country_label"),
                         missing_age = canreg_missing_age(dt_CI5),
                         pop_base_count = "REFERENCE_COUNT",
                         age_label_list = "AGE_GROUP_LABEL")
  
  dt_CI5 <- as.data.table(dt_CI5)
  
  #keep CI5 selected region
  dt_CI5 <- dt_CI5[cr==registry_region,]
  dt_CI5[, cr:=NULL]
  
  #add CI5 data to canreg data
  dt_temp <- unique(dt[,c("ICD10GROUP", "ICD10GROUPCOLOR", "cancer_label"),  with=FALSE])
  dt_CI5 <- merge(dt_CI5,dt_temp, by=c("ICD10GROUP"),all.x=TRUE )
  setnames(dt_CI5, "cases", "CASES")
  setnames(dt_CI5, "py", "COUNT")
  dt[, country_label:=registry_label]
  dt <- rbind(dt,dt_CI5)
  
  return(dt)
  
  
}



canreg_report_template_extract <- function(report_path,script.basename, appendix=FALSE) {
  
  # Copy template base if no file in the report_template folder
  
  # detect txt file beginning with 1_, 12_, 2_ etc.. 
  file_template <- list.files(path=report_path, pattern="^((APP_)?\\d{1,2}_).*\\.txt$")
  
  
  if (length(file_template) == 0) {
    
    file_template <- list.files(path=paste(sep="/", script.basename, "report_text"), full.names = TRUE)
    file.copy(file_template,report_path)
    
  }
  
  # Copy 1.1 chapter if missing
  if (!any(grepl("^1_1_",file_template ))) {
    
    file_1_1 <- list.files(path=paste(sep="/", script.basename, "report_text"),pattern= "^1_1_[^_]*\\.txt$", full.names = TRUE)
    file.copy(file_1_1,report_path)
    
  }
  
  # Copy 1.2 chapter if missing
  if (!any(grepl("^1_2_",file_template ))) {
    
    file_1_2 <- list.files(path=paste(sep="/", script.basename, "report_text"),pattern= "^1_2_[^_]*\\.txt$", full.names = TRUE)
    file.copy(file_1_2,report_path)
    
  }
  
  # Force Appendix first chapter
  
  file_APP_1 <- list.files(path=paste(sep="/", script.basename, "report_text"),pattern= "^APP_1_Graph*\\.txt$", full.names = TRUE)
  file.copy(file_APP_1,report_path)
    
  file_APP_1_1 <- list.files(path=paste(sep="/", script.basename, "report_text"),pattern= "^APP_1_1_Age-specific incidence rate*\\.txt$", full.names = TRUE)
  file.copy(file_APP_1_1,report_path)
    
  
  if (appendix) {
    pattern_template <- "^(APP_\\d{1,2}_).*\\.txt$"
  } else {
    pattern_template <- "^(\\d{1,2}_).*\\.txt$"
  }
  

  
  # detect txt file beginning with 1_, 12_, 2_ etc.. 
  file_template <- list.files(path=report_path, pattern=pattern_template)
  
  if (length(file_template) > 0) {
    dt_chapter <- canreg_report_chapter_table(file_template)
    if (appendix) {
      
      dt_chapter[, title_number:=gsub("APP", "",title_number)]
    }
  } else {
    dt_chapter <- NULL
  }
  

  
  return(dt_chapter)
  
}



canreg_report_chapter_table <- function(file) {
  
  # find  position of first "_" followed by at least 2 non-numeric and not "_" character) 
  pos_name <- gregexpr("_[^0-9_]{2,}.*?\\.txt$", file)
  pos_name <- sapply(pos_name, `[[`, 1)
  
  # extract title_number part
  title_number <- substr(file, 0,pos_name)
  
  #extract title
  title <- substring(file,pos_name+1, nchar(file)-4)
  
  #extract title level
  title_level <- sapply(gregexpr("_", title_number), length)
  
  #create data table with information
  dt_chapter <- data.table(file,title,title_level )
  
  #determine title order
  dt_chapter[,title_number:=gsub("_", "", title_number)]
  setkey(dt_chapter,title_number )
  dt_chapter[,rank:=1:.N]   
  
  #Drop 1.3+ chapter
  dt_chapter <- dt_chapter[!grepl("^[1][3-9]",dt_chapter$title_number )]
  
  
  return(dt_chapter)
  
  
}


canreg_report_chapter_txt <- function(dt_chapter, doc, folder, dt_all, list_number, appendix=FALSE) {
  
  doc <- addPageBreak(doc) # go to the next page
  
  if (!appendix) {
    doc <- addTitle(doc, paste(dt_chapter$title[1], tolower(dt_chapter$title[2]), sep= " and "), level=1)
  } else {
    doc <- addTitle(doc, "Appendix", level=1)
  }
  
  last_landscape <- FALSE
  
  for (i in 1:nrow(dt_chapter)) {
    
    
    doc_landscape <- FALSE
    
    chapter_info <- dt_chapter[i]
    
    text <- scan(paste0(folder,"/", chapter_info$file), what="character", sep="\n", blank.lines.skip = FALSE, quiet=TRUE)
    
    if (length(text) > 0) {
      

      if (substr(text[1], 1,11) == "<LANDSCAPE>") {
        doc_landscape <- TRUE
      }
    
    }

    if (chapter_info$title_level == 1 ) {
      doc <- addPageBreak(doc)
    }
      
    if (doc_landscape) {
      text <- text[2:length(text)]
      doc <- addSection(doc, landscape = TRUE)
      last_landscape <- TRUE
      
    } else {
      if (last_landscape){
        doc <- addSection(doc, landscape = FALSE)
        last_landscape <- FALSE
      }
    }
      
    doc <- addTitle(doc, chapter_info$title, level=chapter_info$title_level)
      
    pop_data <- ((i==2) & !appendix)
      
    if (length(text) > 0) {  
      list_number <- canreg_report_import_txt(doc,text,folder, dt_all, list_number,pop_data, appendix)
    }
  }
  
  return(list_number)
  
}


canreg_report_import_txt <- function(doc,text,folder, dt_all, list_number, pop_data=FALSE, appendix=FALSE) {
  
  
  if (length(text) > 1){
    for (i in 2:length(text)) {
      text[1] <- paste(text[1], text[i], sep = "\n")
    }
  }
  
  text <- text[1]
  
  
  
  #create markup table 
  mark_pos <- NULL
  mark_length <- NULL
  
  mark_table <- data.table(mark_pos=integer(),mark_length=integer(),mark_type=character())
  
  temp <- gregexpr("<EDIT FILE PATH>", text = text)[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("PATH", length(temp)))))
  
  temp <- gregexpr("<EDIT MAP PATH>", text = text)[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("MAP", length(temp)))))
  
  temp <- gregexpr("<POPULATION DATA>", text = text)[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("POP", length(temp)))))
  
  temp <- gregexpr("<AGE_SPECIFIC_RATE_DETAILLED>", text = text)[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("ASRD", length(temp)))))
  
  temp <- gregexpr("\\<img:(\\[(.*)\\])?[^[:space:]]*?\\.png\\>", text = tolower(text))[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("IMG", length(temp)))))
  
  temp <- gregexpr("\\<tbl:(\\[(.*)\\])?[^[:space:]]*?\\.png\\>", text = tolower(text))[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("TBL", length(temp)))))
  
  temp <- gregexpr("\\<img:(\\[(.*)\\])?[^[:space:]]*?\\.jpe?g\\>", text = tolower(text))[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("IMG", length(temp)))))
  
  temp <- gregexpr("\\<img:(\\[(.*)\\])?[^[:space:]]*?\\.bmp\\>", text = tolower(text))[[1]]
  mark_table <- rbindlist(list(mark_table, list(temp, attr(temp,"match.length"),rep("IMG", length(temp)))))
  
  

  
  setkey(mark_table, mark_pos)
  mark_table <- mark_table[mark_pos!=-1, ]
  
  
  if (pop_data ) {
    if (!"POP" %in% mark_table$mark_type) {
      mark_table <- rbind(mark_table,list(nchar(text),17,"POP"))
    }
  }
  
  
  list_number <- canreg_report_add_text(doc,text,mark_table,dt_all, folder, list_number, appendix )
  
  return(list_number)
  
}

canreg_report_add_text <- function(doc, text, mark_table,dt_all, folder, list_number, appendix=FALSE) {
  
  if (nrow(mark_table)==0) { #no markup
    
    if (!is.na(text)) {
      doc <- addParagraph(doc,text) 
    } 
    
  } else {
    
    start <- 0 

    
    for (i in 1:nrow(mark_table)) { 
      

      stop <- mark_table$mark_pos[i]-1 #markup position
      temp <- substr(text, start,stop) # add text before markup
      type <- mark_table$mark_type[i]
      
  
      
      if (temp != "") {
        doc <- addParagraph(doc,temp) 
      }
      

      
      if (type == "PATH") {
        
        temp <- paste0("If you want to keep changes for future reports, this text can be edit directly in the template file folder:\n",folder,"\n")
        doc <- addParagraph(doc,temp) 
        
      } else if (type == "MAP") {
        
        temp <- paste0("If you want to keep changes for future reports, this map can be updated directly in the template file folder:\n",folder,"\\map_example.png\n")
        doc <- addParagraph(doc,temp) 
        
      } else if (type == "POP"){
        
        dt_report <- dt_all
        dt_report <- canreg_pop_data(dt_report)
        doc <- addParagraph(doc, "\r\n")
        
        total_pop <- formatC(round(unique(dt_report$Total)), format="d", big.mark=",") 
        total_male <- formatC(round(dt_report[SEX==levels(SEX)[1], sum(COUNT)]), format="d", big.mark=",")
        total_female <- formatC(round(dt_report[SEX==levels(SEX)[2], sum(COUNT)]), format="d", big.mark=",")
        
        doc <- addParagraph(doc,
                            paste0("The average annual population was ", total_pop,
                                   " (",total_male," males and ",total_female, " females).\n"))
        
        canreg_output(output_type = "png", filename =  paste0(tempdir(), "\\temp_graph"),landscape = TRUE,list_graph = FALSE,
                      FUN=canreg_population_pyramid,
                      df_data =dt_report,
                      canreg_header = "")
        
        dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph.png")), "dim" )
        doc <- addImage(doc, paste0(tempdir(), "\\temp_graph.png"),width=graph_width,height=graph_width*dims[1]/dims[2] )
        doc <- addParagraph(doc,  paste0("Fig ",list_number$fig,". Estimated average annual population"))
        list_number$fig <- list_number$fig+1
        
      } else if (type == "ASRD"){
        
        
        
        doc <- addParagraph(doc, "\r\n")
        dt_report <- dt_all
        
        dt_report <- canreg_ageSpecific_rate_data(dt_report)
        
        canreg_output(output_type = "png", filename = paste0(tempdir(), "\\temp_graph"),landscape = FALSE,
                      list_graph = TRUE,
                      FUN=canreg_ageSpecific_rate_multi_plot,dt=dt_report,group_by="SEX",var_age_label_list = "AGE_GROUP_LABEL",
                      logscale = TRUE,  
                      color_trend=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                      multi_graph= FALSE,
                      canreg_header=ls_args$header)
        
        
        dims <- attr( png::readPNG (paste0(tempdir(), "\\temp_graph001.png")), "dim" )
        
        for (j in 1:length(levels(dt_report$ICD10GROUP))) {
          doc <- addImage(doc, paste0(tempdir(), "\\temp_graph",sprintf("%03d",j) ,".png"),width=graph_width*0.8,height=graph_width*0.8*dims[1]/dims[2] )
          doc <- addParagraph(doc,  
                              paste0("Appendix fig ",list_number$fig,". ", unique(dt_report[ICD10GROUP== levels(ICD10GROUP)[j] ,cancer_label]),  
                                     ": Age-specific incidence rate per ", formatC(100000, format="d", big.mark=",")))
          list_number$fig <- list_number$fig+1
        }
        
        
      } else if (type %in% c("IMG", "TBL")) {
        
        table <- type == "TBL"
        doc <- addParagraph(doc, "\r\n")
        
        caption_markdown <- regexpr("\\[(.*)\\]", substr(text, stop+1,stop+1+mark_table$mark_length[i] ))

        
        if (caption_markdown[1] > 0) {
          img_file <-  substr(text, stop+5+attr(caption_markdown,"match.length"),stop+mark_table$mark_length[i])
          caption  <-  substr(text, stop+6,stop+3+attr(caption_markdown,"match.length"))
          
          
        } else {
          img_file <-  substr(text, stop+5,stop+mark_table$mark_length[i])
          caption <- img_file
        }
        

        if (file_test("-f",paste0(folder, "\\", img_file))) {
          
          
          if (table) {
            caption_start <- paste0("Table ", list_number$tbl, ". ")
            list_number$tbl <- list_number$tbl + 1
            graph_width <- 6
          } else {
            caption_start <- paste0("Fig ", list_number$fig, ". ")
            list_number$fig <- list_number$fig + 1
            graph_width <- 3
          }
          
          if (appendix) {
            caption_start <- paste0("Appendix ", tolower(caption_start))
            
            if (table) {
              graph_width <- 8
            }
          } 

          
          #change extension to low cases in a temp file
          file.copy(paste0(folder, "\\", img_file), paste0(tempdir(),"\\", tolower(img_file)))
          
          
          file_ext <- tolower(regmatches(img_file, regexpr("[^\\.]*$",img_file )))
          
          #"Valid files are png, jpg, jpeg,bmp (add gif,wmf,emf ?)
          if (grepl("jpe?g$",file_ext)) {
            dims <- attr(jpeg::readJPEG(paste0(tempdir(),"\\", tolower(img_file))), "dim" )
          } else if (file_ext == "png") {
            dims <- attr(png::readPNG(paste0(tempdir(),"\\", tolower(img_file))), "dim" )
          } else if (file_ext == "bmp") {
            dims <- attr(bmp::read.bmp(paste0(tempdir(),"\\", tolower(img_file))), "dim" )
          }
          
          doc <- addImage(doc, paste0(tempdir(),"\\", tolower(img_file)),width=graph_width,height=graph_width*dims[1]/dims[2],par.properties = parProperties(text.align = "left"))
          doc <- addParagraph(doc, paste0(caption_start,caption))

          
        } else {
          temp <- paste0("The file: ",folder,"\\",img_file," does not exist\n")
          doc <- addParagraph(doc,temp)
          
        }
      }
      
      start <- stop + mark_table$mark_length[i]+1
    }
    
    
    
    
    temp <- substr(text, start ,nchar(text)) # add text after markup
    
    if (temp != "") {
      doc <- addParagraph(doc,temp) 
    }
    
  }
  
  return(list_number)
  
}


canreg_report_top_cancer_text <- function(dt_report, percent_equal=5, sex_select="Male") {
  
  dt_temp <- as.data.table(dt_report)
  dt_temp <- dt_temp[SEX==sex_select]
  dt_temp[, cancer_rank:= frank(-CASES, ties.method="first")]
  
  setkeyv(dt_temp, c("cancer_rank"))
  
  #  rank for second set
  temp <- dt_temp[cancer_rank==1,CASES]
  dt_temp[, pct_temp:=(temp-CASES)/temp*100]
  dt_temp[pct_temp<=percent_equal, rank:=1]
  
  #  rank for second set
  dt_temp[, cancer_rank:=NA] #first set of cancer
  dt_temp[is.na(rank), cancer_rank:= frank(-CASES, ties.method="first")] #first set of cancer
  temp <- dt_temp[cancer_rank==1,CASES]
  dt_temp[is.na(rank) , pct_temp:=(temp-CASES)/temp*100]
  dt_temp[is.na(rank) & pct_temp<=percent_equal, rank:=2]
  
  dt_temp <- dt_temp[rank<=2]
  
  label1 <- dt_temp[rank==1,cancer_label]
  cases1 <- dt_temp[rank==1,CASES]
  label2 <- dt_temp[rank==2,cancer_label]
  cases2 <- dt_temp[rank==2,CASES]
  
  label_1 <- label1[1]
  cases_1<- formatC(cases1[1], format="d", big.mark=",")
  
  if (length(label1) > 1) {
    for (i in 2:length(label1)) {
      
      if (i != length(label1)) {
        
        label_1 <- paste0(label_1,", ",label1[i])
        cases_1 <- paste0(cases_1,", ",formatC(cases1[i], format="d", big.mark=","))
        
      } else {
        
        label_1 <- paste0(label_1," and ",label1[i])
        cases_1 <- paste0(cases_1," and ",formatC(cases1[i], format="d", big.mark=","))
      }
    }
    
    label_1 <- paste0(label_1," are ")
    
  } else {
    
    label_1 <- paste0(label_1," is ")
  }
  
  cases_1 <- paste0(cases_1," cases")
  text2 <- paste0(label2[1], " (",formatC(cases2[1], format="d", big.mark=","), " cases)")
  
  if (length(label2) > 1) {
    for (i in 2:length(label2)) {
      
      if (i != length(label2)) {
        
        text2 <- paste0(text2,", ",label2[i], " (",formatC(cases2[i], format="d", big.mark=","), " cases)")
        
      } else {
        
        text2 <- paste0(text2," and ",label2[i], " (",formatC(cases2[i], format="d", big.mark=","), " cases)")
        
      }
    }
  }
  
  
  
  text <-paste0(label_1,"the most commonly diagnosed malignancy with ",cases_1,
                ", followed by ",text2,".")
  return(text)
}


canreg_basis_table <- function(dt,var_cases="CASES", var_basis="BASIS", var_cancer_label="cancer_label", var_ICD="ICD10GROUP") {
  
  dt <- as.data.table(dt)
  setnames(dt, var_cases, "CSU_C")
  setnames(dt, var_cancer_label, "CSU_label")
  setnames(dt, var_ICD, "CSU_ICD")
  
  
  dt <-  dt[,list( CSU_C=sum(CSU_C)), by=c(var_basis,"CSU_label", "CSU_ICD")]
  dt_total <- dt[,list( CSU_C=sum(CSU_C)), by=var_basis]
  dt_total[, CSU_label:="All sites"]
  dt_total[, CSU_ICD:="All"]
  dt <- rbind(dt, dt_total)
  
  dt[, total_cases := sum(CSU_C), by=c("CSU_label", "CSU_ICD")]
  dt[, BASIS_pc := CSU_C/total_cases*100]
  dt[,BASIS_pc:=round(BASIS_pc,1)]
  dt[, BASIS_pc:=format(BASIS_pc, big.mark = ",", scientific = FALSE, drop0trailing = FALSE)]
  dt[, CSU_C := NULL]
  
  dt <- reshape(dt, timevar = "BASIS",idvar = c("CSU_label","CSU_ICD","total_cases"), direction = "wide")
  dt[, total_pc_test:= total_cases/sum(total_cases)*200]
  dt[,total_pc_test:=round(total_pc_test,1)]
  dt[, total_pc_test:=format(total_pc_test, big.mark = ",", scientific = FALSE, drop0trailing = FALSE)]
  setkeyv(dt, c("CSU_ICD"))
  
  if(!("BASIS_pc.0" %in% colnames(dt)))
  {
    dt[, BASIS_pc.0:="0.0"]
  }
  
  if(!("BASIS_pc.1" %in% colnames(dt)))
  {
    dt[, BASIS_pc.1:="0.0"]
  }
  
  if(!("BASIS_pc.2" %in% colnames(dt)))
  {
    dt[, BASIS_pc.2:="0.0"]
  }
  
  setcolorder(dt, c("CSU_label", "CSU_ICD", "total_cases", "total_pc_test", "BASIS_pc.0", "BASIS_pc.1","BASIS_pc.2"))
  
  return(dt)
}



csu_merge_inc_pop <- function(inc_file,
                              pop_file,
                              var_cases = "CASES",
                              var_age = "AGE_GROUP",
                              var_age_label = "AGE_GROUP_LABEL",
                              var_pop = "COUNT",
                              var_ref_count = "REFERENCE_COUNT",
                              group_by = NULL,
                              column_group_list = NULL){
  
  df_inc <- read.table(inc_file, header=TRUE)
  df_pop <- read.table(pop_file, header=TRUE)
  
  dt_inc <- data.table(df_inc)
  dt_pop <- data.table(df_pop)
  
  setnames(dt_inc, var_cases, "CSU_C")
  
  column_group_list[[1]]  <- intersect(column_group_list[[1]],colnames(dt_inc))
  group_by <- intersect(group_by,colnames(dt_inc))
  
  dt_inc <- dt_inc[, c(var_age, group_by, "CSU_C"), with = FALSE]
  dt_inc <-  dt_inc[,list(CSU_C = sum(CSU_C)), by=eval(colnames(dt_inc)[!colnames(dt_inc) %in% c("CSU_C")])]
  
  if (!is.null(column_group_list)){
    cj_var <- colnames(dt_inc)[!colnames(dt_inc) %in% unlist(c("CSU_C",lapply(column_group_list, `[`, -1)))]
  } else {
    cj_var <-colnames(dt_inc)[!colnames(dt_inc) %in% c("CSU_C")]
  }
  
  dt_temp = dt_inc[, do.call(CJ, c(.SD, unique=TRUE)), .SDcols=cj_var]
  
  ##keep ICD group label
  if (!is.null(column_group_list)){
    nb_group <- length(column_group_list)
    for( i in 1:nb_group) {
      dt_col_group <- unique(dt_inc[, column_group_list[[i]], with=FALSE])
      dt_temp <- merge(dt_temp, dt_col_group,by= column_group_list[[i]][[1]], all.x=TRUE)
    }
  }
  
  dt_inc <- merge(dt_temp, dt_inc,by=colnames(dt_temp), all.x=TRUE)[, CSU_C := ifelse(is.na(CSU_C),0, CSU_C )]
  
  if (nrow(dt_inc[!SEX %in% c(1,2)]) > 0){
    
    var_group2 <- "ICD10GROUP"
    if ("BASIS" %in% group_by) {
      var_group2 <- c(var_group2,"BASIS" )
    }
    
    dt_inc <- canreg_attr_missing_sex(dt_inc, var_age, var_group2)
  }
  
  dt_pop <- dt_pop[get(var_pop) != 0,]
  dt_pop[[var_ref_count]] <-  dt_pop[[var_ref_count]]*100
  
  dt_all <- merge(dt_inc, dt_pop,by=intersect(colnames(dt_inc),colnames(dt_pop)), all.x=TRUE)
  
  #create ICD10color if not existing (take care of NA when using the color) and add cancer_label
  dt_all$cancer_label <- canreg_cancer_info(dt_all)$cancer_label
  if (!"ICD10GROUPCOLOR" %in% colnames(dt_all)) {
    
    dt_color_map <- csu_cancer_color(unique(canreg_cancer_info(dt_all)$cancer_label))
    dt_all <- merge(dt_all, dt_color_map, by = c("cancer_label"), all.x=TRUE, sort=F )
  }
  
  setnames(dt_all,var_age,"CSU_A")
  setnames(dt_all,var_pop,"CSU_P")
  
  
  dt_all[is.na(get(var_age_label)), CSU_A := max(CSU_A)]
  dt_all <-  dt_all[,list(CSU_C = sum(CSU_C), CSU_P = sum(CSU_P)), by=eval(colnames(dt_all)[!colnames(dt_all) %in% c("CSU_C", "CSU_P")])]

  setnames(dt_all,"CSU_P",var_pop)
  setnames(dt_all,"CSU_A",var_age)
  setnames(dt_all,"CSU_C",var_cases)
  return(dt_all)
}



canreg_attr_missing_sex <- function(dt, var_age, var_group2) {
  
  
  
  
  #drop row with no missing value 
  dt <- dt[SEX %in% c(1,2) | CSU_C>0, ]
  # create counter 0
  dt[ , counter0 := .GRP, by = c(var_age, "YEAR", var_group2)]
  
  # keep only usefull column: CSU cases & COUNTER & SEX, AGE, YEAR,"ICD10GROUP", "BASIS"?
  dt_temp <- dt[, c("CSU_C", "counter0", "SEX",var_age, "YEAR", var_group2 ), with=FALSE]
  
  #create sub counter for age and ICD10GROUP
  dt_temp[ ,  counter1 := .GRP, by = c(var_age, var_group2)]
  dt_temp[ ,  counter2 := .GRP, by = c(var_group2)]
  
  # calculate nb_known for the 3 counter
  dt_temp[SEX %in% c(1,2), nb_known2:= sum(CSU_C), by=counter2]
  dt_temp[!SEX %in% c(1,2), nb_known2:= 0]
  dt_temp[ , nb_known2:= max(nb_known2), by=counter2]
  dt_temp <- dt_temp[nb_known2 > 0,] # drop if no known cases for the all site
  
  dt_temp[SEX %in% c(1,2), nb_known:= sum(CSU_C), by=counter0]
  dt_temp[SEX %in% c(1,2), nb_known1:= sum(CSU_C), by=counter1]
  
  # calculate proba_male for each subgroup
  dt_temp[SEX==1 & nb_known >0, p_male:= sum(CSU_C)/nb_known, by=counter0]
  dt_temp[SEX==1  & nb_known1>0 , p_male1:= sum(CSU_C)/nb_known1, by=counter1]
  dt_temp[SEX==1  & nb_known2>0 , p_male2:= sum(CSU_C)/nb_known2, by=counter2]
  
  #keep more precise proba
  dt_temp[is.na(p_male1) , p_male1:= p_male2]
  dt_temp[is.na(p_male) , p_male:= p_male1]
  
  #keep only useful variable
  dt_temp[, c("p_male1", "p_male2", "counter1", "nb_known","nb_known1","nb_known2",var_age,"YEAR",var_group2) := NULL]
  
  #create column for nb missing and drop if nb_missing = 0
  dt_temp[!SEX %in% c(1,2), nb_missing:= sum(CSU_C), by=counter0]
  dt_temp[SEX %in% c(1,2), nb_missing:= 0]
  dt_temp[  ,nb_missing:=max(nb_missing), by=counter0 ]
  dt_temp <- dt_temp[nb_missing>0,]
  
  # Distribute the missing sex cases based on probability
  #dt_temp[SEX==1, nb_add:=sum(rbinom(nb_missing,1,p_male)), by=counter0]
  dt_temp[SEX==1, nb_add:=round(nb_missing*p_male), by=counter0]
  dt_temp[, nb_add:=max(nb_add,na.rm=TRUE), by=counter0]
  dt_temp[SEX==2, nb_add:=nb_missing-nb_add]
  # update CSU_CASES
  dt_temp[, CSU_C:=CSU_C+nb_add, ]
  #keep only usefull variable
  dt_temp <- dt_temp[SEX %in% c(1,2), c("CSU_C", "counter0", "SEX")]
  
  #merge by counter0 & sex 
  dt <- merge(dt, dt_temp, by=c("counter0","SEX"), all=T, suffixes=c("",".new"))
  dt[CSU_C.new > CSU_C ,CSU_C:=CSU_C.new]
  dt[,"CSU_C.new":=NULL]
  dt <- dt[SEX %in% c(1,2), ]
  
  return(dt)
  
}



canreg_desc_missing_sex <- function(inc_file,
                                    var_cases = "CASES"){
  
  df_inc <- read.table(inc_file, header=TRUE)
  dt_inc <- data.table(df_inc)
  setnames(dt_inc, var_cases, "CSU_C")
  nb_total <- sum(dt_inc[,CSU_C])
  nb_missing <- sum(dt_inc[!SEX %in% c(1,2),CSU_C])
  percent_missing <- round(nb_missing/nb_total*100)
  
  return(list(nb_total=nb_total,nb_missing=nb_missing,percent_missing=percent_missing))
  
}


canreg_ageSpecific_rate_data <- function(dt, keep_ref=FALSE, keep_year=FALSE, keep_basis = FALSE) { 
  
  group_by <- c("cancer_label","ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "AGE_GROUP","AGE_GROUP_LABEL", "SEX")
  if (keep_ref) {
    group_by <- c(group_by, "REFERENCE_COUNT")
  }
  
  if (keep_year) {
    group_by <- c(group_by, "YEAR")
  }
  
  if (keep_basis) {
    group_by <- c(group_by, "BASIS")
  }
  
  dt <-  dt[AGE_GROUP != canreg_missing_age(dt) ,list(CASES=sum(CASES), COUNT=sum(COUNT)), by=group_by]
  dt$cancer_sex <- canreg_cancer_info(dt)$cancer_sex
  dt$cancer_title <- paste(dt$cancer_label, "\n(", dt$ICD10GROUP, ")", sep="")
  dt$SEX <- factor(dt$SEX, levels=c(1,2), labels=c("Male", "Female"))
  dt <- dt[, -c("ICD10GROUPLABEL"), with=FALSE]
  dt <- dt[!((substring(cancer_sex, 1, 1) ==0) & SEX == "Male"),]
  dt <- dt[!((substring(cancer_sex, 2, 2) ==0) & SEX == "Female"),]
  
  dt[, ICD10GROUP :=factor(ICD10GROUP)]
  
  return(dt) 
}

canreg_age_cases_data <- function(dt, age_group = c(15,30,50,70), skin=FALSE) {
  
  #drop missing value
  dt <- dt[AGE_GROUP != canreg_missing_age(dt),]
  
  dt[, first:= as.numeric(substring(AGE_GROUP_LABEL,0,regexpr("[^[:alnum:]]",AGE_GROUP_LABEL)-1)) ]
  dt[, last:= as.numeric(substring(AGE_GROUP_LABEL,regexpr("[^[:alnum:]]",AGE_GROUP_LABEL)+1)) ]
  dt[is.na(last), last:= 1000L ]
  dt[, age_cut:=findInterval(last,age_group)]
  dt[, first:=min(first), by=age_cut]
  dt[, last:=max(last), by=age_cut]
  dt[, group_label:=paste0(as.character(first),"-",as.character(last))]
  dt[, group_label:= gsub("-1000", "+", group_label)]
  
  #drop skin if no ALL but skin
  if (!skin) {
    dt <- dt[ICD10GROUP == "O&U",] 
  }
  
  dt <- dt[,list(CASES=sum(CASES)), by=c("SEX", "group_label", "age_cut")]
  
  #add sex label
  dt$SEX <- factor(dt$SEX, levels=c(1,2), labels=c("Male", "Female"))
  return(dt)
}

canreg_year_cases_data <- function(dt, var_year="YEAR", skin=FALSE, missing_age = FALSE) {
  
  #drop missing value
  if (missing_age) dt <- dt[AGE_GROUP != canreg_missing_age(dt),]
  #drop skin if no ALL but skin
  if (!skin) dt <- dt[ICD10GROUP == "O&U",]
  dt <- dt[,list(CASES=sum(CASES)), by=var_year]
  return(dt)
}

canreg_pop_data <- function(dt) {
  
  dt_pop <- dt[, .(AGE_GROUP, YEAR, SEX, COUNT, AGE_GROUP_LABEL)]
  dt_pop <- unique(dt_pop)
  dt_pop <- dt_pop[!is.na(AGE_GROUP_LABEL),]
  dt_pop <- dt_pop[,.(COUNT=mean(COUNT)), by=.(AGE_GROUP,SEX,AGE_GROUP_LABEL)]
  dt_pop[,Total:=sum(COUNT)]
  dt_pop[,Percent:=COUNT/sum(COUNT)*100, by=SEX]
  dt_pop[,Percent:=round(Percent,1)]
  dt_pop$SEX <- factor(dt_pop$SEX, levels=c(1,2), labels=c("Male", "Female"))
  return(dt_pop)
  
}


canreg_get_agegroup_label <- function(dt, agegroup) {
  
  
  first_age <- as.numeric(substr(agegroup,1,regexpr("-", agegroup)[1]-1))
  last_age <- as.numeric(substr(agegroup,regexpr("-", agegroup)[1]+1,nchar(agegroup)))
  
  temp_max <- max(dt[!is.na(AGE_GROUP_LABEL),]$AGE_GROUP)
  temp_min <- min(dt[!is.na(AGE_GROUP_LABEL),]$AGE_GROUP)
  if (temp_max < last_age) {
    last_age <- temp_max
  } 
  if (temp_min > first_age) {
    first_age <- temp_min  
  } 
  temp1 <- as.character(unique(dt[dt$AGE_GROUP == first_age,]$AGE_GROUP_LABEL))
  temp2 <-as.character(unique(dt[dt$AGE_GROUP == last_age,]$AGE_GROUP_LABEL))
  temp1 <- substr(temp1,1,regexpr("-", temp1)[1]-1)
  temp2 <- substr(temp2,regexpr("-", temp2)[1]+1,nchar(temp2))
  
  return(list(first_age = first_age, last_age= last_age, label = paste0(temp1,"-",temp2, " years")))
}



csu_cum_risk_core <- function(df_data, var_age, var_cases, var_py, group_by=NULL,
                              missing_age = NULL,age_label_list = NULL,last_age = 15,
                              var_cum_risk="cum_risk") {
  
  
  
  bool_dum_by <- FALSE
  
  if (is.null(group_by)) {
    
    df_data$CSU_dum_by <- "dummy_by"
    group_by <- "CSU_dum_by"
    bool_dum_by <- TRUE
    
  }
  

  
  
  dt_data <- data.table(df_data, key = group_by) 
  setnames(dt_data, var_age, "CSU_A")
  setnames(dt_data, var_cases, "CSU_C")
  setnames(dt_data, var_py, "CSU_P")
  
  
  # create index to keep order
  index_order <- c(1:nrow(dt_data))
  dt_data$index_order <- index_order
  
  # missing age 
  dt_data[dt_data$CSU_A==missing_age,CSU_A:=NA ] 
  dt_data[is.na(dt_data$CSU_A),CSU_P:=0 ] 
  
  #create age dummy: 1 2 3 4 --- 19
  dt_data$age_factor <- c(as.factor(dt_data$CSU_A))

  
  # correction factor 
  dt_data$correction <- 1 
  if (!is.null(missing_age)) {
    
    
    dt_data[, total:=sum(CSU_C), by=group_by] #add total
    dt_data[!is.na(dt_data$age_factor) , total_known:=sum(CSU_C), by=group_by] #add total_know
    dt_data$correction <- dt_data$total / dt_data$total_know 
    dt_data[is.na(dt_data$correction),correction:=1 ] 
    dt_data$total <- NULL
    dt_data$total_known <- NULL
    
    dt_data<- dt_data[!is.na(age_factor),]
    
  }
  

  
  if (!is.null(age_label_list)) {
  # calcul year interval from age group label
  
    dt_temp <- unique(dt_data[, c(age_label_list), with=FALSE])
    dt_temp[, min:=as.numeric(regmatches(get(age_label_list), regexpr("[0-9]+",get(age_label_list))))]
    dt_temp[, max:=shift(min, type ="lead")]
    dt_temp[, age_span := max-min]
    dt_temp <- dt_temp[, c("age_span",age_label_list), with=FALSE]
    dt_data <- merge(dt_data, dt_temp,by= age_label_list, all.x=TRUE)
  } else {
    
    dt_data[, age_span:=5]
  }

  
  #keep age group selected 
  

  
  age_max <- max(dt_data$age_factor)

  if (age_max-1 < last_age) {
    last_age <- age_max-1 
  }
  dt_data=dt_data[dt_data$age_factor <= last_age]  
  


  dt_data[,cum_risk:=age_span*(CSU_C/CSU_P)]
  dt_data[CSU_P==0,cum_risk:=0]
  

  
  # to check order 
  dt_data<- dt_data[order(dt_data$index_order ),]
  dt_data <- dt_data[,list( cum_risk=sum(cum_risk), CSU_P=sum(CSU_P),CSU_C=sum(CSU_C),correction = max(correction)), by=group_by]
  dt_data[,cum_risk:=(1-exp(-cum_risk))*100*correction]
  
  dt_data[,cum_risk:=round(cum_risk, digits = 2)]
  dt_data[, correction:=round((correction-1)*100, digits = 1)]
  
  
  if (var_cum_risk!="cum_risk") {
    setnames(dt_data, "cum_risk", var_cum_risk)
  }
  
  dt_data$correction <- NULL
  
  setnames(dt_data, "CSU_C", var_cases)
  setnames(dt_data,  "CSU_P", var_py)
  
  if (bool_dum_by) {
    df_data$CSU_dum_by <- NULL
  }
  
  #temp <- last_age*5-1
  #cat("Cumulative risk have been computed for the age group 0-", last_age*5-1 , "\n",  sep="" )
  
  
  return(dt_data)
  
}



csu_trend_legend <-
  function(title=NULL, position="bottom",nrow=1, right_space_margin=1) {
    
    structure(list(title = title, position = position,
                   nrow = nrow,
                   right_space_margin = right_space_margin))
  }



canreg_ageSpecific_rate_multi_plot <- function(dt, 
                                               var_age = "AGE_GROUP",
                                               var_cases= "CASES",
                                               var_py= "COUNT",
                                               group_by="SEX",
                                               var_age_label_list = "AGE_GROUP_LABEL",
                                               color_trend=c("Male" = "#08519c", "Female" = "#a50f15"),
                                               logscale=FALSE,
                                               multi_graph=TRUE,
                                               list_graph=TRUE,
                                               landscape = FALSE,
                                               return_data = FALSE,
                                               canreg_header=canreg_header) {
  
  
  if (return_data) {
    dt[, rate := CASES/COUNT*10000]
    dt[, cancer_sex := NULL]
    dt[, cancer_title := NULL]
    dt[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
    dt <- dt[, c("cancer_label",
                 "ICD10GROUP",
                 "SEX",
                 "AGE_GROUP",
                 "AGE_GROUP_LABEL",
                 "CASES",
                 "COUNT",
                 "rate"), with=FALSE]
    setkeyv(dt, c("ICD10GROUP", "SEX","AGE_GROUP"))
    return(dt)
    stop() 
  }
  
  ## use a fonction depending on row and col  for lay, theme, widths, heights
  
  lay <- rbind(c(NA,16,16,16,16),
               c(13,1,2,3,4),  
               c(13,5,6,7,8),
               c(13,9,10,11,12),
               c(NA,14,14,14,14),
               c(NA,15,15,15,15))  
  
  theme_3p4 <- list(theme(
    axis.title.x = element_blank(), 
    axis.title.y = element_blank(),
    legend.position="none",  ## use a fonction depending on row and col
    axis.text = element_text(size=8, colour = "black"),
    axis.ticks = element_line(size=0.25),
    axis.ticks.length = unit(0.1, "cm"),
    axis.line.x = element_line(size = 0.25),
    axis.line.y = element_line(size = 0.25),
    line = element_line(size = 0.25),
    plot.title = element_blank(),
    plot.caption = element_blank(),
    plot.margin=margin(3,3,3,3),
  )
  )
  
  
  
    if (landscape) {
    
    theme_orient <- list(theme(axis.text.x = element_text(size=8, angle = 60,  hjust = 1),
                               plot.subtitle = element_text(size=12)
    )
    )
    axis_text_y_log <- 5
    
    
    widths <- c(1,5,5,5,5)
    heights <- c(1,5,5,5,1,1)
    
  } else {
    
    theme_orient <- list(theme(axis.text.x = element_text(size=6, angle = 60,  hjust = 1),
                               plot.subtitle = element_text(size=10)
    )
    )
    widths <- c(1,5,5,5,5)
    heights <- c(1,6,6,6,1,1)
    
    axis_text_y_log <- 6
    
  }
  
  if (logscale){
    
    theme_log <- list(theme(axis.text.y = element_text(size=axis_text_y_log)))
    
  } else {
    
    theme_log <- list(theme(axis.text.y = element_text(size=8)))
    
  }
  
  if (multi_graph) {
    
    plotlist_grid <- list()
    
  }
  
  if (list_graph) {
    
    plotlist <- list()
    
  }
  
  j <- 1
  
  for ( i in levels(dt$ICD10GROUP) ) { 
    
    
    
    
    dt_temp <- dt[ICD10GROUP ==i]
    
    cancer_title <- unique(dt_temp$cancer_title)
    temp <- Rcan:::core.csu_ageSpecific(dt_temp,
                                 var_age=var_age,
                                 var_cases= var_cases,
                                 var_py=var_py,
                                 group_by = group_by,
                                 plot_title = canreg_header,
                                 plot_subtitle = cancer_title,
                                 plot_caption = canreg_header,
                                 color_trend = color_trend,
                                 logscale = logscale,
                                 age_label_list = unique(dt[[var_age_label_list]])
    )$csu_plot
    
    if (list_graph) {
      
      plotlist[[j]] <- temp + list(theme(plot.title = element_blank()))
      
    }
    
    
    if (multi_graph) {
      
      if (j==1) {
        
        grid_legend <- extract_legend_axes(temp)
      }
      
      temp <- temp  + theme_orient + theme_log + theme_3p4
      
      
      plotlist_grid[[j]] <- temp
      geom_line_index <- which(sapply(plotlist_grid[[j]]$layers, function(x) class(x$geom)[1]) == "GeomLine")
      plotlist_grid[[j]]$layers[[geom_line_index]]$aes_params$size <- 0.5
      
      
      if (logscale) {
        geom_point_index <- which(sapply(plotlist_grid[[j]]$layers, function(x) class(x$geom)[1]) == "GeomPoint")
        plotlist_grid[[j]]$layers[[geom_point_index]]$aes_params$size <-1
        plotlist_grid[[j]]$layers[[geom_point_index]]$aes_params$stroke <-0.25
      }
      
    }
    
    
    
    j <- j+1
    
  }
  
  
  if (multi_graph) {
    
    
    
    
    plotlist_grid[[j]] <- grid_legend$ylab
    plotlist_grid[[j+1]] <- grid_legend$xlab
    plotlist_grid[[j+2]] <- grid_legend$legend
    plotlist_grid[[j+3]] <- grid_legend$title
    
   
    
    grid.arrange(
      grobs=plotlist_grid,
      layout_matrix = lay,
      widths = widths,
      heights=heights,
      left=" ",
      top= " ",
      bottom= " ",
      right= " "
    )
    
  }
  
  
  if (list_graph) {
    
    for (i in 1:length(plotlist)) {
      
      geom_line_index <- which(sapply(plotlist[[i]]$layers, function(x) class(x$geom)[1]) == "GeomLine")
      plotlist[[i]]$layers[[geom_line_index]]$aes_params$size <- 1
      plotlist[[i]] <- plotlist[[i]] +
        guides(color = guide_legend(override.aes = list(size=0.75)))
      
      if (logscale) {
        geom_point_index <- which(sapply(plotlist[[i]]$layers, function(x) class(x$geom)[1]) == "GeomPoint")
        plotlist[[i]]$layers[[geom_point_index]]$aes_params$size <-3
        plotlist[[i]]$layers[[geom_point_index]]$aes_params$stroke <-0.5
      }
      
      print(plotlist[[i]])
    }
    
  }
  
}


canreg_age_cases_pie_multi_plot <- function(dt, 
                                            var_cases= "CASES",
                                            var_bar  = "group_label",
                                            color_age=c("#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854"),
                                            list_graph=FALSE,
                                            landscape = TRUE,
                                            return_data = FALSE,
                                            canreg_header=NULL) {
  
  
  
  if (return_data) {
    dt[, age_cut :=NULL]
    dt[, percent:=sum(get(var_cases)), by="SEX"]
    dt[, percent:=get(var_cases)/percent*100]
    setkeyv(dt, c("SEX", "group_label"))
    return(dt)
    stop() 
  }
  
  lay <- rbind(c(4,4),
               c(1,2),
               c(3,3))
  
  theme_3p4 <- list(theme(
    legend.position="none",  ## use a fonction depending on row and col
    line = element_line(size = 0.25),
    plot.title = element_blank(),
    plot.margin=margin(0,0,0,0),
  )
  )
  
  widths <- c(1,1)
  heights <- c(1,4,1)
  
  plotlist_grid <- list()
  
  j <- 1
  
  for ( i in levels(dt$SEX) ) { 
    
    dt_temp <- dt[SEX ==i]
    temp <- canreg_cases_age_pie(df_data = dt_temp,
                                 var_cases = var_cases,
                                 var_bar = var_bar,
                                 color_age = color_age,
                                 list_graph = list_graph,
                                 plot_subtitle = i,
                                 canreg_header = canreg_header

    )
    if (j==1) {
      
      grid_legend <- extract_legend_axes(temp)
    }
    
    temp <- temp  + theme_3p4
    plotlist_grid[[j]] <- temp
    
    j <- j+1
    
  }
  
  
  plotlist_grid[[j]] <- grid_legend$legend
  plotlist_grid[[j+1]] <- grid_legend$title


 
	grid.arrange(
	  grobs=plotlist_grid,
	  layout_matrix = lay,
	  widths = widths,
	  heights=heights,
	  left=" ",
	  top= " ",
	  bottom= " ",
	  right= " "
	)

 
  
  
}


canreg_ageSpecific_rate_top <- function(df_data, 
										var_age="AGE_GROUP",
										var_cases= "CASES", 
                                        var_py= "COUNT",
                                        group_by="SEX",
										var_top = "cancer_label",
                                        var_age_label_list = "AGE_GROUP_LABEL",
										var_color="ICD10GROUPCOLOR",
										logscale = TRUE,
										nb_top = 5,
										plot_title=NULL,
										landscape = FALSE,
									    list_graph = FALSE,
										return_data = FALSE) {
		 
if (return_data) {
	dt_data <- Rcan:::core.csu_dt_rank(df_data, var_value = var_cases, var_rank = var_top ,group_by = group_by, number = nb_top) 		 
	dt_data[, rate := CASES/COUNT*10000]
    dt_data[, cancer_sex := NULL]
    dt_data[, cancer_title := NULL]
    dt_data[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
    setnames(dt_data, "CSU_RANK","cancer_rank")
    dt_data <- dt_data[, c(var_top,
                 "ICD10GROUP",
                 "cancer_rank",
                 group_by,
                 var_age,
                 var_age_label_list,
                 var_cases,
                 var_py,
                 "rate"), with=FALSE]
    setkeyv(dt_data, c(group_by,"cancer_rank","ICD10GROUP" ,var_age ))
    return(dt_data)
    stop() 
}

plot_subtitle <- paste0("Top ",nb_top, " cancer sites" )

temp <- Rcan:::core.csu_ageSpecific_top(df_data,var_age, var_cases, var_py,var_top, group_by,
									   logscale=logscale, 
									   nb_top=nb_top, 
									   plot_title=plot_title,
									   plot_subtitle=plot_subtitle,
									   var_color=var_color,
									   var_age_label_list=var_age_label_list,
									   caption_bypass=TRUE)

for (i in  1:length(temp$plotlist)) {
  
  print(temp$plotlist[[i]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))

}


}





canreg_bar_top_single <- function(dt, var_top, var_bar = "cancer_label" ,group_by = "SEX",
                                  nb_top = 10, landscape = FALSE,list_graph=TRUE,
                                  canreg_header = "", xtitle = "",digit  =  1,
                                  return_data  =  FALSE) {
  
  dt <- Rcan:::core.csu_dt_rank(dt, var_value = var_top, var_rank = var_bar,group_by = group_by, number = nb_top) 
  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    setkeyv(dt, c(group_by,"cancer_rank"))
    dt <-  dt[,-c("ICD10GROUPCOLOR"), with=FALSE]
    
    return(dt)
    stop() 
  }
  
  dt$cancer_label <-Rcan:::core.csu_legend_wrapper(dt$cancer_label, 15)
  
  plotlist <- list()
  j <- 1 
  
  for (i in levels(dt[[group_by]])) {
    
    if (j == 1) {
      plot_title <- canreg_header
      plot_caption <- ""
    } else {
      plot_title <- ""
      plot_caption <- canreg_header
    }
    
    plot_subtitle <-  paste0("Top ",nb_top, " cancer sites\n",i)
    
    dt_plot <- dt[get(group_by) == i]
    dt_label_order <- setkey(unique(dt_plot[, c(var_bar,"ICD10GROUPCOLOR", "CSU_RANK"), with=FALSE]), CSU_RANK)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = rev(dt_label_order$cancer_label)) 
    color_cancer <- as.character(rev(dt_label_order$ICD10GROUPCOLOR))
    

    
    plotlist[[j]] <-
      csu_bar_plot(
        dt_plot,var_top=var_top,var_bar=var_bar,
        plot_title=plot_title,plot_caption=plot_caption,plot_subtitle = plot_subtitle,
        color_bar=color_cancer,
        landscape=landscape,digit=digit,
        xtitle=xtitle)
    
    print(plotlist[[j]])
    j <- j+1
    
  }
}


canreg_bar_CI5_compare <- function(dt,group_by = "SEX", landscape = TRUE,list_graph=TRUE,multi_graph=FALSE,
                                        xtitle = "",digit  =  1,text_size_factor =1.5,number=5,
                                        return_data  =  FALSE) {
  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    dt <-  dt[,-c("ICD10GROUPCOLOR"), with=FALSE]
    
    return(dt)
    stop() 
  }
  
  
  CI5_registries <- as.character(unique(dt$country_label))
  caption <- NULL
  if (any(grepl("\\*",CI5_registries))) {
    caption <- "*: Regional registries"
  }
  
  lay <- rbind(c(11,12),
               c(1,2),
               c(3,4),  
               c(5,6),
               c(7,8),
               c(9,9),
               c(NA,10))  
  
  theme_2p4 <- list(theme(
    axis.title.x = element_blank(), 
    axis.title.y = element_blank(),
    axis.text = element_text(colour = "black"),
    axis.text.x = element_text(size=10),
    axis.text.y = element_text(size=9),
    axis.line.x = element_line(size = 0.25),
    panel.grid.major.x = element_line(size=0.25),
    panel.grid.minor.x = element_line(size=0.25),
    axis.ticks.x = element_line(size=0.25),
    plot.title = element_text(size=12),
    plot.subtitle = element_blank(),
    plot.caption = element_blank(),
    plot.margin=margin(0,3,0,0),
  )
  )
  
  widths <- c(10,10)
  heights <- c(1,10,10,10,10,1,1)
  
  
  if (multi_graph) {
    
    plotlist_grid <- list()
    
  }
  
  if (list_graph) {
    
    plotlist <- list()
    
  }
  
  i <- 1 
  
  
  for (j in 1:number) {
    
    dt_temp <- dt[CSU_RANK ==j ]
    
    for (k in levels(dt_temp[[group_by]])) local({
      
      k <- k
      dt_plot <- dt_temp[get(group_by) == k]
      
      dt_plot[["country_label"]] <-Rcan:::core.csu_legend_wrapper(dt_plot[["country_label"]], 14)
      dt_plot[,country_label:=factor(country_label, levels=country_label)]
      
      
      
      
      temp <-
        csu_bar_plot(dt=dt_plot, 
                     var_top="asr",
                     var_bar="country_label",
                     plot_title = unique(dt_plot$cancer_label),
                     plot_subtitle = unique(dt_plot$SEX), 
                     plot_caption = caption,
                     xtitle=xtitle,
                     digit = digit,
                     color_bar = as.character(dt_plot$ICD10GROUPCOLOR),
                     text_size_factor = text_size_factor,
                     landscape = TRUE) 
      
      if (list_graph) {
        plotlist[[i]] <<- temp
      }
      
      if (multi_graph) {
        
        if (i==1) { 
          grid_legend <<- extract_legend_axes(temp)
          plotlist_grid[[11]] <<- grid_legend$subtitle
        }
        
        if (i == 2) {
          grid_legend <<- extract_legend_axes(temp)
          plotlist_grid[[12]] <<- grid_legend$subtitle
        } 
        
        if (i < 11) {
          
          temp <- temp  + theme_2p4
          plotlist_grid[[i]] <<- temp
          geom_text_index <- which(sapply(plotlist_grid[[i]]$layers, function(x) class(x$geom)[1]) == "GeomText")
          plotlist_grid[[i]]$layers[[geom_text_index]]$aes_params$size <<- 3.5
          geom_hline_index <- which(sapply(plotlist_grid[[i]]$layers, function(x) class(x$geom)[1]) == "GeomHline")
          plotlist_grid[[i]]$layers[[geom_hline_index]]$aes_params$size <<- 0.25
        }
      }
      
      
      i <<- i+1
      
    })
  }
  
  if (multi_graph) {
    
    #need edit to keep only 10 
    
    plotlist_grid[[9]] <- grid_legend$xlab
    plotlist_grid[[10]] <- grid_legend$caption
    
    
    
    grid.arrange(
      grobs=plotlist_grid,
      layout_matrix = lay,
      widths = widths,
      heights=heights,
      left=" ",
      top= " ",
      bottom= " ",
      right= " "
    )
    
  }
  
  if (list_graph) {
    
    for (i in 1:length(plotlist)) {
      
      geom_text_index <- which(sapply(plotlist[[i]]$layers, function(x) class(x$geom)[1]) == "GeomText")
      plotlist[[i]]$layers[[geom_text_index]]$aes_params$size <- 6 
      geom_hline_index <- which(sapply(plotlist[[i]]$layers, function(x) class(x$geom)[1]) == "GeomHline")
      plotlist[[i]]$layers[[geom_hline_index]]$aes_params$size <- 0.4
      
      print(plotlist[[i]])
    }
  }
  
  
}

csu_bar_plot <- function(dt, 
                             var_top,
                             var_bar,
                             plot_title = plot_title,
                             plot_subtitle = "", 
                             plot_caption = NULL,
                             xtitle="",
                             digit = 1,
                             color_bar = NULL,
                             text_size_factor = 1,
                             landscape = FALSE)  {
  
  line_size <- 0.4
  text_size <- 14 
  title_size <- 18
  subtitle_size <- 16
  caption_size <- 12
  
  if (landscape) {
    csu_ratio = 0.6
    csu_bar_label_size = 4
  } else {
    csu_ratio = 1.4
    csu_bar_label_size = 5
  }
  
  text_size <- text_size*text_size_factor
  csu_bar_label_size <- csu_bar_label_size*text_size_factor
  title_size <- title_size*text_size_factor
  subtitle_size <- subtitle_size*text_size_factor
  caption_size <- caption_size*text_size_factor
  
  setnames(dt,var_top,"plot_value")
  
  tick_major_list <- Rcan:::core.csu_tick_generator(max = max(dt$plot_value), 0)$tick_list
  nb_tick <- length(tick_major_list) 
  tick_space <- tick_major_list[nb_tick] - tick_major_list[nb_tick-1]
  if ((tick_major_list[nb_tick] -  max(dt$plot_value))/tick_space < 1/4){
    tick_major_list[nb_tick+1] <- tick_major_list[nb_tick] + tick_space
  }
  
  tick_minor_list <-seq(tick_major_list[1],tail(tick_major_list,1),tick_space/2)
  

  dt$value_label <- dt$plot_value + (tick_space*0.1)
  dt$value_round <-  format(round(dt$plot_value, digits = digit), nsmall = digit)
  
  csu_plot <- 
    ggplot(dt, aes(get(var_bar), plot_value, fill=get(var_bar))) +
    geom_bar(stat="identity", width = 0.8)+
    geom_hline(yintercept = 0, colour="black",size = line_size)+
    geom_text(aes(get(var_bar), value_label,label=value_round),
              size = csu_bar_label_size,
              hjust = 0)+
    coord_flip(ylim = c(0,tick_major_list[length(tick_major_list)]+(tick_space*0.25)), expand = TRUE)+
    scale_y_continuous(name = xtitle,
                       breaks=tick_major_list,
                       minor_breaks = tick_minor_list,
                       labels=Rcan:::core.csu_axes_label
                       
    )+
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    labs(title = plot_title, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.y= element_blank(),
      panel.grid.major.x= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=title_size, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=subtitle_size, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=caption_size, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_blank(),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_blank(),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      legend.position = "none",
    )
  
  return(csu_plot)
  
}


canreg_bar_top <- function(df_data,
                               var_top = "asr",
                               var_bar = "cancer_label",
                               group_by = "SEX",
                               nb_top = 10,
                               color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                               landscape = FALSE,
                               list_graph = FALSE,
                               canreg_header=NULL,
                               ytitle = "",
                               nsmall = 1,
                               return_data = FALSE,
                               plot_caption= NULL) {
  
  
  dt <- data.table(df_data)
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 4
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 14
  
  setnames(dt, var_top, "CSU_ASR")
  setnames(dt, var_bar, "CSU_BAR")
  setnames(dt, group_by, "CSU_BY")
  
  plot_subtitle <- paste("top",nb_top,"cancer sites")
  
  dt <- Rcan:::core.csu_dt_rank(dt, var_value = "CSU_ASR", var_rank = "CSU_BAR",number = nb_top)
  
  
  
  if (return_data) {
    dt[, rank_value := NULL]
    setnames(dt, "CSU_BAR",var_bar)
    setnames(dt, "CSU_BY", group_by)
    setnames(dt, "CSU_ASR", var_top)
    setnames(dt, "CSU_RANK","cancer_rank")
    setkeyv(dt, c("cancer_rank",var_bar))
    return(dt)
    stop() 
  }
  
  
  
  dt$CSU_BAR <-Rcan:::core.csu_legend_wrapper(dt$CSU_BAR, 15)
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], asr_plot:= CSU_ASR*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], asr_plot:= CSU_ASR]
  
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  factor_order <- unique(dt[, c("CSU_BAR", "CSU_RANK"), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = rev(setkeyv(factor_order, "CSU_RANK")$CSU_BAR)) 
  
  tick_minor_list <- Rcan:::core.csu_tick_generator(max = max(dt$CSU_ASR), 0)$tick_list
  nb_tick <- length(tick_minor_list) 
  tick_space <- tick_minor_list[nb_tick] - tick_minor_list[nb_tick-1]
  if ((tick_minor_list[nb_tick] -  max(dt$CSU_ASR))/tick_space < 1/4){
    tick_minor_list[nb_tick+1] <- tick_minor_list[nb_tick] + tick_space
  }
  tick_major <- tick_minor_list[1:length(tick_minor_list) %% 2  == 1]
  tick_major_list <- c(rev(-tick_major),tick_major[tick_major!=0])
  tick_label <- c(rev(tick_major),tick_major[tick_major!=0])
  tick_minor_list <- c(rev(-tick_minor_list),tick_minor_list[tick_minor_list!=0])
  
  dt$asr_label <- dt$CSU_ASR + (tick_space*0.1)
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], asr_label:= asr_label*(-1)]
  
  dt$asr_round <-  format(round(dt$CSU_ASR, digits = 1), nsmall = nsmall)
  
  csu_plot <- ggplot(dt, aes(CSU_BAR, asr_plot, fill=CSU_BY)) +
    geom_bar(stat="identity", width = 0.8)+
    geom_hline(yintercept = 0, colour="black",size = line_size)+
    geom_text(data=dt[asr_label > 0, ],aes(CSU_BAR, asr_label,label=asr_round),
              size = csu_bar_label_size,
              hjust = 0)+
    geom_text(data=dt[asr_label < 0, ],aes(CSU_BAR, asr_label,label=asr_round),
              size = csu_bar_label_size,
              hjust = 1)+
    coord_flip(ylim = c(tick_minor_list[1]-(tick_space*0.25),tick_minor_list[length(tick_minor_list)]+(tick_space*0.25)), expand = TRUE)+
    scale_y_continuous(name = ytitle,
                       breaks=tick_major_list,
                       minor_breaks = tick_minor_list,
                       labels=tick_label
    )+
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    labs(title = canreg_header, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.y= element_blank(),
      panel.grid.major.x= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_blank(),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_blank(),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      legend.key = element_rect(fill="transparent"),
      legend.position = "bottom",
      legend.text = element_text(size = text_size),
      legend.key.height = unit(0.6,"cm"),
      legend.key.width =unit(1.5,"cm"),
      legend.margin = margin(0, 0, 0, 0)
    )
  

    print(csu_plot)

  
}

canreg_population_pyramid <- function(df_data,
                                      var_cases = "Percent",
                                      var_bar = "AGE_GROUP_LABEL",
                                      group_by = "SEX",
                                      var_age_cut="AGE_GROUP",
                                      color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                                      landscape = FALSE,
                                      list_graph = FALSE,
                                      canreg_header=NULL,
                                      return_data = FALSE,
                                      plot_caption= NULL) {
  
  
  
  dt <- as.data.table(df_data)
  
  if (return_data) {
    dt[, c(var_age_cut) := NULL]
    return(dt)
    stop() 
  }
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 5
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 14
  
  setnames(dt, var_cases, "CSU_CASES")
  setnames(dt, var_bar, "CSU_BAR")
  setnames(dt, group_by, "CSU_BY")
  
  dt$CSU_BY <- factor(dt$CSU_BY)
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], cases_plot:= CSU_CASES*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], cases_plot:= CSU_CASES]
  
  factor_order <- unique(dt[, c("CSU_BAR", var_age_cut), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = setkeyv(factor_order, var_age_cut)$CSU_BAR) 
  
  tick_minor_list <- Rcan:::core.csu_tick_generator(max = max(dt$CSU_CASES), 0)$tick_list
  nb_tick <- length(tick_minor_list) 
  tick_space <- tick_minor_list[nb_tick] - tick_minor_list[nb_tick-1]
  
  if ((tick_minor_list[nb_tick] -  max(dt$CSU_CASES))/tick_space < 1/4){
    tick_minor_list[nb_tick+1] <- tick_minor_list[nb_tick] + tick_space
  }
  
  tick_major <- tick_minor_list[1:length(tick_minor_list) %% 2  == 1]
  tick_major_list <- c(rev(-tick_major),tick_major[tick_major!=0])
  tick_label <- c(rev(tick_major),tick_major[tick_major!=0])
  tick_minor_list <- c(rev(-tick_minor_list),tick_minor_list[tick_minor_list!=0])
  
  
  dt_poly1 <- dt[AGE_GROUP == max(AGE_GROUP),.(AGE_GROUP, cases_plot, CSU_BY)] 
  dt_poly2 <- dt_poly1
  dt_poly2$cases_plot <- 0
  dt_poly3 <- dt_poly2
  dt_poly3$AGE_GROUP =  dt_poly3$AGE_GROUP+1
  dt_poly <- rbind(dt_poly1,dt_poly2,dt_poly3)
  dt_poly[,AGE_GROUP :=AGE_GROUP+0.5]
  dt_poly[AGE_GROUP == max(AGE_GROUP),AGE_GROUP :=AGE_GROUP+1]
  
  dt[AGE_GROUP == max(AGE_GROUP), cases_plot :=0 ]
  
  total_pop <- unique(dt$Total) 
  i <- 1000
  j <- 1
  while (total_pop >= i & i <= 100000) {
    i=i*10
    j=j*10
  }
  total_pop = round(total_pop/j)*j
  
  csu_plot <- ggplot(dt, aes(CSU_BAR, cases_plot, fill=CSU_BY)) +
    geom_bar(stat="identity", width = 1, colour="black")+
    geom_polygon(data=dt_poly, aes(x=AGE_GROUP, y=cases_plot, group=CSU_BY), colour="black" )+
    geom_hline(yintercept = 0, colour="black",size = line_size)+
    coord_flip(ylim = c(tick_minor_list[1]-(tick_space*0.25),tick_minor_list[length(tick_minor_list)]+(tick_space*0.25)), expand = TRUE)+
    scale_y_continuous(name = "% of total population",
                       breaks=tick_major_list,
                       minor_breaks = tick_minor_list,
                       labels=tick_label
    )+
    scale_x_discrete(name = "")+
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    labs(title = canreg_header, 
         subtitle = paste("Population:", formatC( total_pop, format="d", big.mark=",")),
         caption = plot_caption)+
  theme(
    aspect.ratio = csu_ratio,
    plot.background= element_blank(),
    panel.background = element_blank(),
    panel.grid.major.y= element_blank(),
    panel.grid.major.x= element_line(colour = "grey70",size = line_size),
    panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
    plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
    plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
    plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
    plot.margin=margin(20,20,20,20),
    axis.title = element_text(size=text_size),
    axis.title.x=element_text(margin=margin(10,0,0,0)),
    axis.title.y = element_text(margin=margin(0,10,0,0)),
    axis.text = element_text(size=text_size, colour = "black"),
    axis.text.x = element_text(size=text_size),
    axis.text.y = element_text(size=text_size),
    axis.ticks.x= element_line(colour = "black", size = line_size),
    axis.ticks.y= element_blank(),
    axis.ticks.length = unit(0.2, "cm"),
    axis.line.y = element_blank(),
    axis.line.x = element_line(colour = "black", 
                               size = line_size, 
                               linetype = "solid"),
    legend.key = element_rect(fill="transparent"),
    legend.position = "bottom",
    legend.text = element_text(size = text_size),
    legend.key.height = unit(0.6,"cm"),
    legend.key.width =unit(1.5,"cm"),
    legend.margin = margin(0, 0, 0, 0)
  )
  

  print(csu_plot)





}

canreg_cases_year_bar <- function(dt,
                                  var_cases = "CASES",
                                  var_bar = "YEAR",
                                  skin=FALSE,
                                  landscape = FALSE,
                                  list_graph = FALSE,
                                  canreg_header=NULL,
                                  return_data = FALSE,
                                  plot_caption= NULL) {
  
  
  dt <- as.data.table(dt)
  
  if (return_data) {
    return(dt)
    stop() 
  }
  
  
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 5
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 18
  if (skin) {
    plot_subtitle <- paste("All cancers")
  } else {
    plot_subtitle <- paste("All cancers but C44")
  }
  
  
  setnames(dt, var_cases, "CSU_CASES")
  setnames(dt, var_bar, "CSU_BAR")
  
  
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  tick_major_list <- Rcan:::core.csu_tick_generator(max = max(dt$CSU_CASES), 0)$tick_list
  nb_tick <- length(tick_major_list) 
  tick_space <- tick_major_list[nb_tick] - tick_major_list[nb_tick-1]
  if ((tick_major_list[nb_tick] -  max(dt$CSU_CASES))/tick_space < 1/4){
    tick_major_list[nb_tick+1] <- tick_major_list[nb_tick] + tick_space
  }
  
  tick_minor_list <-seq(tick_major_list[1],tail(tick_major_list,1),tick_space/2)
  
  dt$cases_label <- dt$CSU_CASES + (tick_space*0.1)
  
  csu_plot <- ggplot(dt, aes(CSU_BAR, CSU_CASES)) +
    geom_bar(stat="identity", width = 0.8, fill="#1673ce")+
    geom_text(data=dt,aes(CSU_BAR, cases_label,label=CSU_CASES),
              size = csu_bar_label_size,
              vjust = 0)+
    coord_cartesian( 
      xlim = c(0.4, nrow(dt)+ 0.6),
      ylim = c(0,tick_major_list[length(tick_major_list)]+(tick_space*0.25)),
      expand = FALSE
    )+
    scale_y_continuous(name = "Number of cases",
                       breaks=tick_major_list,
                       minor_breaks = tick_minor_list
    )+
    scale_x_discrete(name = "Year")+
    labs(title = canreg_header, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.x= element_blank(),
      panel.grid.major.y= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.y= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_text(margin=margin(0,15,0,0)),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      
    )
  

    print(csu_plot)

  
}

canreg_cases_age_bar <- function(df_data,
                                     var_cases = "CASES",
                                     var_bar = "group_label",
                                     group_by = "SEX",
                                     var_age_cut="age_cut",
                                     color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                                     landscape = FALSE,
                                     list_graph = FALSE,
                                     canreg_header=NULL,
                                     return_data = FALSE,
                                     skin=TRUE,
                                     plot_caption= NULL) {
  
  
  dt <- as.data.table(df_data)
  
  if (return_data) {
    dt[, c(var_age_cut) := NULL]
    return(dt)
    stop() 
  }
  
  
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 5
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 14
  if (skin) {
    plot_subtitle <- paste("All cancers")
  } else {
    plot_subtitle <- paste("All cancers but C44")
  }
  
 
  
  setnames(dt, var_cases, "CSU_CASES")
  setnames(dt, var_bar, "CSU_BAR")
  setnames(dt, group_by, "CSU_BY")
  
  dt$CSU_BY <- factor(dt$CSU_BY)
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], cases_plot:= CSU_CASES*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], cases_plot:= CSU_CASES]
  
  factor_order <- unique(dt[, c("CSU_BAR", var_age_cut), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = setkeyv(factor_order, var_age_cut)$CSU_BAR) 
  
  tick_minor_list <- Rcan:::core.csu_tick_generator(max = max(dt$CSU_CASES), 0)$tick_list
  nb_tick <- length(tick_minor_list) 
  tick_space <- tick_minor_list[nb_tick] - tick_minor_list[nb_tick-1]
  
  if ((tick_minor_list[nb_tick] -  max(dt$CSU_CASES))/tick_space < 1/4){
    tick_minor_list[nb_tick+1] <- tick_minor_list[nb_tick] + tick_space
  }
  
  tick_major <- tick_minor_list[1:length(tick_minor_list) %% 2  == 1]
  tick_major_list <- c(rev(-tick_major),tick_major[tick_major!=0])
  tick_label <- c(rev(tick_major),tick_major[tick_major!=0])
  tick_minor_list <- c(rev(-tick_minor_list),tick_minor_list[tick_minor_list!=0])
  
  dt$cases_label <- dt$CSU_CASES + (tick_space*0.1)
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], cases_label:= cases_label*(-1)]
  
  csu_plot <- ggplot(dt, aes(CSU_BAR, cases_plot, fill=CSU_BY)) +
    geom_bar(stat="identity", width = 0.8)+
    geom_hline(yintercept = 0, colour="black",size = line_size)+
    geom_text(data=dt[cases_label > 0, ],aes(CSU_BAR, cases_label,label=CSU_CASES),
              size = csu_bar_label_size,
              hjust = 0)+
    geom_text(data=dt[cases_label < 0, ],aes(CSU_BAR, cases_label,label=CSU_CASES),
              size = csu_bar_label_size,
              hjust = 1)+
    coord_flip(ylim = c(tick_minor_list[1]-(tick_space*0.25),tick_minor_list[length(tick_minor_list)]+(tick_space*0.25)), expand = TRUE)+
    scale_y_continuous(name = "Number of cases",
                       breaks=tick_major_list,
                       minor_breaks = tick_minor_list,
                       labels=tick_label
    )+
    scale_x_discrete(name = "Age Group")+
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    labs(title = canreg_header, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.y= element_blank(),
      panel.grid.major.x= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_text(margin=margin(0,10,0,0)),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_blank(),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      legend.key = element_rect(fill="transparent"),
      legend.position = "bottom",
      legend.text = element_text(size = text_size),
      legend.key.height = unit(0.6,"cm"),
      legend.key.width =unit(1.5,"cm"),
      legend.margin = margin(0, 0, 0, 0)
    )
  
    print(csu_plot)

  
}


canreg_cases_age_pie <- function(
                        df_data = dt_all,
                        var_cases = "CASES",
                        var_bar  =  "group_label",
                        color_age = c("#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854"),
                        list_graph  =  FALSE,
                        plot_subtitle = "Male",
                        canreg_header  = NULL) {
  
  dt <- as.data.table(df_data)
  dt[, percent:=sum(get(var_cases))]
  dt[, percent:=get(var_cases)/percent]
  

  
  setnames(dt, var_cases, "CSU_CASES")
  setnames(dt, var_bar, "CSU_BAR")
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  factor_order <- unique(dt[, c("CSU_BAR", "age_cut"), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = rev(setkeyv(factor_order, "age_cut")$CSU_BAR)) 
  
  
  dt[, x_label:=1]
  dt[percent < 0.1,x_label:=1.6]
  dt[percent >= 0.1 & percent < 0.2,x_label:=1.2]
  
  
  dt[, y_label := cumsum(CSU_CASES) - 0.5*CSU_CASES]
  dt[shift(percent) < 0.04 & percent <0.04, y_label:=y_label + 0.25*CSU_CASES ]
  dt[percent < 0.01,y_label:=y_label - 0.5*CSU_CASES]
  
  dt[,text_size:=2]
  dt[percent < 0.1,text_size:=1]
  dt[percent < 0.01,text_size:=0]
  


  
  
  color_age <- c("#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854")
  color_age <- color_age[1:length(levels(dt$CSU_BAR))]
  
  csu_plot <- 
    ggplot(data = dt, aes(x = "", y = CSU_CASES, fill = CSU_BAR)) + 
    geom_bar(width = 1, stat = "identity") +
    geom_text(aes(label = percent(percent), x=x_label, y= y_label, size=text_size), show.legend=FALSE) +
    coord_polar(theta = "y") +
    scale_fill_manual(name = "Age Group", values = color_age) + 
    scale_size_continuous(range=c(4,6))+
    labs(title = canreg_header, 
         subtitle = plot_subtitle)+
    theme(plot.background= element_blank(),
          plot.title = element_text(size=18, margin=margin(0,0,0,0),hjust = 0.5),
          plot.subtitle = element_text(size=16, margin=margin(0,0,0,0),hjust = 0.5),
          panel.background = element_blank(),
          axis.text.x=element_blank(),
          axis.title.x = element_blank(),
          axis.title.y = element_blank(),
          panel.border = element_blank(),
          panel.grid=element_blank(),
          legend.position = "bottom",
          legend.key = element_rect(fill="transparent"),
          legend.title = element_text(size = 16),
          legend.text = element_text(size = 16),
          legend.key.height = unit(0.8,"cm"),
          legend.key.width =unit(1.2,"cm"),
          legend.margin = margin(0, 0, 0, 0),
          axis.ticks = element_blank()) 
    

    csu_plot <- csu_plot + guides(fill=guide_legend(reverse = TRUE))

    
  return(csu_plot)
  
}




canreg_asr_trend_top <- function(dt, var_asr="asr", 
                                 var_cases= "CASES", 
                                 var_year= "YEAR",
                                 group_by="cancer_label",
                                 logscale = TRUE,
                                 number = 5,
                                 ytitle=NULL,
                                 landscape = FALSE,
                                 list_graph = FALSE,
                                 return_data = FALSE,
                                 canreg_header="") {
  
  
  dt <- Rcan:::core.csu_dt_rank(dt,
                    var_value= var_cases, 
                    var_rank = group_by,
                    group_by = "SEX",
                    number = number
                    )

  


  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    dt <- dt[, c("SEX",group_by,"cancer_rank",var_year,var_asr), with=FALSE]
    setkeyv(dt, c("SEX","cancer_rank",var_year))
    return(dt)
    stop() 
  }
  
  #wrap label for legend
  dt[[group_by]] <-Rcan:::core.csu_legend_wrapper(dt[[group_by]], 14)
  
  plotlist <- list()
  j <- 1 
  for (i in levels(dt[["SEX"]])) {
    
    if (j == 1) {
      canreg_header <- canreg_header
      plot_caption <- ""
    } else {
      canreg_header <- ""
      plot_caption <- canreg_header
    }
    

    
    dt_plot <- dt[get("SEX") == i]
    dt_label_order <- setkey(unique(dt_plot[, c(group_by,"ICD10GROUPCOLOR", "CSU_RANK"), with=FALSE]), CSU_RANK)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = dt_label_order$cancer_label) 
    
    color_cancer <- as.character(dt_label_order$ICD10GROUPCOLOR)
    

    
    
    plotlist[[j]] <- Rcan:::core.csu_time_trend(dt_plot,
                                    var_trend = "asr",
                                    var_year = "YEAR",
                                    group_by = "cancer_label",
                                    logscale = logscale,
                                    smoothing = NULL,
                                    ytitle = ytitle,
                                    plot_title = canreg_header,
                                    plot_subtitle = paste0("Top ",number, " cancer sites\n",i),
                                    plot_caption = plot_caption,
                                    color_trend = color_cancer)$csu_plot
    
    j <- j+1
  }
  
  

	print(plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))
	print(plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))

  
}


canreg_eapc_scatter <- function(dt_plot,
                                var_bar = "cancer_label",
                                var_eapc = "eapc",
                                group_by = "SEX",
                                color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                                landscape = FALSE,
                                list_graph = FALSE,
                                canreg_header=NULL,
                                ytitle = "",
                                return_data = FALSE,
                                plot_caption= NULL) {
  
  if (return_data) {
    dt_plot[, CSU_RANK := NULL]
    return(dt_plot)
    stop() 
  }
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 4
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 14
  
  #calcul ticks:
  tick <- Rcan:::core.csu_tick_generator(max = max(dt_plot[[var_eapc]]), min=min(dt_plot[[var_eapc]]))
  
  #to have positive and negative side
  tick_space <- tick$tick_list[length(tick$tick_list)] - tick$tick_list[length(tick$tick_list)-1]
  if (min(tick$tick_list) == 0) {
    tick$tick_list <- c(-tick_space,tick$tick_list)
  }
  if (max(tick$tick_list) == 0) {
    tick$tick_list <- c(tick$tick_list,tick_space)
  }
  
  dt_label_order <- setkeyv(unique(dt_plot[, c(var_bar, var_eapc), with=FALSE]), c(var_eapc))
  dt_plot[[var_bar]] <- factor(dt_plot[[var_bar]],levels = unique(dt_label_order[[var_bar]],fromLast=TRUE)) 
  
  
  setnames(dt_plot, var_eapc, "CSU_EAPC")
  setnames(dt_plot, var_bar, "CSU_BAR")
  setnames(dt_plot, group_by, "CSU_BY")
  
  csu_plot <- 
    ggplot(dt_plot, aes(CSU_EAPC, CSU_BAR)) +
    geom_point(aes(fill =CSU_BY),shape=23, color="black", size=5, stroke = 0.8)+
    scale_x_continuous(name = ytitle,
                       breaks=tick$tick_list,
                       limits=c(tick$tick_list[1],tick$tick_list[length(tick$tick_list)]),
                       labels=Rcan:::core.csu_axes_label)+
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    geom_vline(xintercept = 0, size=0.8)+
    labs(title = canreg_header, 
         subtitle = NULL,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.y= element_line(colour = "grey70",size = line_size/2,linetype="dotted" ),
      panel.grid.major.x= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_blank(),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_blank(),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      legend.key = element_rect(fill="transparent"),
      legend.position = "bottom",
      legend.text = element_text(size = text_size),
      legend.key.height = unit(0.6,"cm"),
      legend.key.width =unit(1.5,"cm"),
      legend.margin = margin(0, 0, 0, 0)
    )
  
  
  
   print(csu_plot)

  
}


canreg_eapc_scatter_error_bar <- function(dt,
                                          var_eapc = "eapc",
                                          var_eapc_up = "eapc_up",
                                          var_eapc_low = "eapc_low",
                                          var_bar = "cancer_label",
                                          group_by = "SEX",
                                          landscape = FALSE,
                                          list_graph = TRUE,
                                          canreg_header=NULL,
                                          return_data = FALSE,
                                          ytitle="") {
  
  if (return_data) {
    
    dt[, CSU_RANK := NULL]
    return(dt)
    stop() 
  }
  
  
  
  #calcul ticks:
  tick <- Rcan:::core.csu_tick_generator(max = max(dt[[var_eapc_up]]), min=min(dt[[var_eapc_low]]))
  
  #to have positive and negative side
  tick_space <- tick$tick_list[length(tick$tick_list)] - tick$tick_list[length(tick$tick_list)-1]
  if (min(tick$tick_list) == 0) {
    tick$tick_list <- c(-tick_space,tick$tick_list)
  }
  if (max(tick$tick_list) == 0) {
    tick$tick_list <- c(tick$tick_list,tick_space)
  }
  
  plotlist <- list()
  j <- 1 
  for (i in levels(dt[[group_by]])) {
    
    plot_title <- canreg_header
    plot_subtitle <-  i
    axe_title = paste0(ytitle, ", ",i)
    
    dt_plot <- dt[get(group_by) == i]
    
    plotlist[[j]] <-
      rcan_scatter_error_bar(
        dt_plot,
        tick_list = tick$tick_list,
        plot_title=plot_title,plot_subtitle = plot_subtitle,
        landscape=landscape,
        ytitle=axe_title)
    
    print(plotlist[[j]])
    j <- j+1
    
  }
}




rcan_scatter_error_bar <- function(dt_plot,
                                   var_bar = "cancer_label",
                                   var_data = "eapc",
                                   var_data_up = "eapc_up",
                                   var_data_low = "eapc_low",
                                   ytitle = "",
                                   landscape = FALSE,
                                   list_graph = TRUE,
                                   plot_title=NULL,
                                   plot_subtitle=NULL,
                                   plot_caption= NULL,
                                   tick_list=NULL) {
  
  
  
  if (landscape) {
    csu_ratio = 0.6 
    csu_bar_label_size = 4
  } else {
    csu_ratio = 1
    csu_bar_label_size = 5 
  }
  
  line_size <- 0.4
  text_size <- 14
  
  
  
  
  
  dt_label_order <- setkeyv(unique(dt_plot[, c(var_bar, var_data), with=FALSE]), c(var_data))
  dt_plot[[var_bar]] <- factor(dt_plot[[var_bar]],levels = unique(dt_label_order[[var_bar]],fromLast=TRUE)) 
  
  
  setnames(dt_plot, var_data, "CSU_DATA")
  setnames(dt_plot, var_data_up, "CSU_DATA_UP")
  setnames(dt_plot, var_data_low, "CSU_DATA_LOW")
  setnames(dt_plot, var_bar, "CSU_BAR")
  
  
  
  csu_plot <- 
    ggplot(dt_plot, aes(CSU_DATA, CSU_BAR)) +
    geom_errorbarh(aes(xmin = CSU_DATA_LOW,xmax = CSU_DATA_UP), size=0.7,height =0.5,colour="#05305b") + 
    geom_point(fill ="#e41a1c",shape=21, color="black", size=3, stroke = 1.2)
  
  if (is.null(tick_list)) {
    
    csu_plot <- csu_plot +
      scale_x_continuous(name = ytitle)
    
  } else {
    
    csu_plot <- csu_plot +
      scale_x_continuous(name = ytitle,
                         breaks=tick_list,
                         limits=c(tick_list[1],tick_list[length(tick_list)]),
                         labels=Rcan:::core.csu_axes_label)
  }
  
  csu_plot <- csu_plot +
    scale_fill_manual(name="",
                      values= color_bar,
                      drop = FALSE)+
    geom_vline(xintercept = 0, size=0.8)+
    labs(title = plot_title, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    theme(
      aspect.ratio = csu_ratio,
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major.y=  element_blank(),
      panel.grid.major.x= element_line(colour = "grey70",size = line_size),
      panel.grid.minor.x= element_line(colour = "grey70",size = line_size),
      plot.title = element_text(size=18, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=12, margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.title = element_text(size=text_size),
      axis.title.x=element_text(margin=margin(10,0,0,0)),
      axis.title.y = element_blank(),
      axis.text = element_text(size=text_size, colour = "black"),
      axis.text.x = element_text(size=text_size),
      axis.text.y = element_text(size=text_size),
      axis.ticks.x= element_line(colour = "black", size = line_size),
      axis.ticks.y= element_blank(),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.y = element_blank(),
      axis.line.x = element_line(colour = "black", 
                                 size = line_size, 
                                 linetype = "solid"),
      legend.key = element_rect(fill="transparent"),
      legend.position = "bottom",
      legend.text = element_text(size = text_size),
      legend.key.height = unit(0.6,"cm"),
      legend.key.width =unit(1.5,"cm"),
      legend.margin = margin(0, 0, 0, 0)
    )
  
  return(csu_plot)
  
}


canreg_output <- function(output_type="pdf",filename=NULL, landscape = FALSE,list_graph = TRUE, FUN,...) {
  
  
  #http://www.altelia.fr/actualites/calculateur-resolution-definition-format.htm

  # 6 inch = 15.24 cm
  #10,795
    
  png_width <- ifelse(landscape, 2339 , 1654 )
  png_height <- ifelse(landscape, 1654 , 2339 )
  png_width_600 <- ifelse(landscape, 3600 , 2549 )
  png_height_600 <- ifelse(landscape, 2549 , 3600 )
  tiff_width <- ifelse(landscape, 3508 , 2480 )
  tiff_height <- ifelse(landscape, 2480 , 3508 )
  svg_width <- ifelse(landscape, 11.692 , 8.267 )
  svg_height <- ifelse(landscape, 8.267 , 11.692 )
  pdf_width <- ifelse(landscape, 11.692 , 8.267 )    
  pdf_height <- ifelse(landscape, 8.267 , 11.692 )   
  file_number <- ifelse(list_graph, "%03d", "")
  

  ## create output plot
  
  
  if (is.null(output_type)) {
    FUN(..., landscape=landscape, list_graph=list_graph)
  } else if (output_type == "ps") {
    postscript(paste0(filename,".ps"),width = svg_width, height = svg_height)
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "png") {
    png(paste0(filename,file_number,".png"),width = png_width, height = png_height, units = "px",res = 200) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "tiff") {
    tiff(paste0(filename,file_number,".tiff"),width = tiff_width, height = tiff_height, units = "px",res = 300,compression ="lzw" ) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  }else if (output_type == "svg") {
    svg(paste0(filename,file_number,".svg"),width = svg_width, height = svg_height,) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  }else if (output_type == "pdf") {
    CairoPDF(file=paste0(filename,".pdf"), width = pdf_width, height = pdf_height) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "csv") {
    df_data <- FUN(..., return_data=TRUE)
    write.csv(df_data, paste0(filename,".csv"),
              row.names = FALSE)
  }  else if (output_type == "jpeg") {
    jpeg(paste0(filename,file_number,".jpeg"),width = tiff_width, height = tiff_height,res = 300, quality = 90) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  }
}




csu_cancer_color <- function(cancer_list) {
  
  
  
  cancer_base <- c("Mouth & pharynx","Lip, oral cavity",
                    "Oesophagus",
                    "Stomach",
                    "Colon, rectum, anus","Colon",
                    "Rectum",
                    "Liver", 
                    "Pancreas", 
                    "Larynx",
                    "Lung, trachea, bronchus","Trachea, bronchus and lung","Lung",
                    "Melanoma of skin",
                    "Breast",
                    "Cervix", "Cervix uteri",
                    "Corpus & Uterus NOS", "Corpus uteri",
                    "Ovary & adnexa","Ovary",
                    "Prostate",
                    "Testis",
                    "Kidney & urinary NOS","Kidney",
                    "Bladder",
                    "Brain & nervous sytem","Brain, nervous system",
                    "Thyroid",
                    "Lymphoma","Non-Hodgkin lymphoma",
                    "Leukaemia",
                    "Non-Melanoma Skin","Other skin",
                    "Ill-defined",
                    "Others and unspecified","Other and unspecified")
  
  ICD10GROUPCOLOR <- c("#AE563E", "#AE563E",
                    "#DC1341", 
                    "#ae56a2",
                    "#FFD803","#FFD803",
                    "#77214c",
                    "#F3A654", 
                    "#940009", 
                    "#6D8A6D",
                    "#1E90FF","#1E90FF","#1E90FF",
                    "#894919",
                    "#FF68BC", 
                    "#FF7500","#FF7500",
                    "#9FE72D", "#9FE72D", 
                    "#8C07C2", "#8C07C2", 
                    "#34A76E",
                    "#4682B4",
                    "#040186","#040186",
                    "#7FF76F", 
                    "#D5BED2","#D5BED2",
                    "#ADD9E4",
                    "#9931D0","#9931D0",
                    "#FFFFA6",
                    "#2A4950","#2A4950",
                    "#278D29",
                    "#DCDCDC","#DCDCDC")
  
  
  dt_color <- data.table(cancer_label = cancer_base, ICD10GROUPCOLOR= ICD10GROUPCOLOR)
  cancer_list <- unique(cancer_list)
  dt_cancer_list <- data.table(cancer_label = cancer_list)
  dt_color_map <- merge(dt_cancer_list, dt_color, by = c("cancer_label"), all.x=TRUE, sort=F )
  
  colours(distinct=TRUE)[c(1:136,235:502)]
  temp <- nrow(dt_color_map[is.na(ICD10GROUPCOLOR),])
  dt_color_map[is.na(ICD10GROUPCOLOR),ICD10GROUPCOLOR:=sample(colours(distinct=TRUE)[c(1:136,235:502)],temp)]

  return(dt_color_map)
  
}


parse_icd10 <- function (icd) {
  
  icd_list <- NULL
  
  #extract XX-XX code
  temp1 <- regmatches(icd, gregexpr("[C|,][0-9]{2}-[0-9]{2}", icd))
  
  if (length(temp1[[1]]) > 0) {
    temp2 <- regmatches(temp1[[1]], gregexpr("[0-9]{2}", temp1[[1]]))
    for (i in 1:length(temp2)) {
      icd_list <- c(icd_list,sapply(temp2[[i]][1]:temp2[[i]][2],
                                    function(x) {
                                      if (nchar(x) == 1) {
                                        x <- paste0("0",x)
                                      }
                                      return(x)
                                    })
      )
    }
  }
  
  #add XX code
  temp1 <- regmatches(icd, gregexpr("[C|,][0-9]{2}", icd))
  
  if (length(temp1[[1]]) > 0) {
    temp2 <- regmatches(temp1[[1]], gregexpr("[0-9]{2}", temp1[[1]]))
    for (i in 1:length(temp2)) {
      icd_list <- c(icd_list,temp2[[i]][1])
    }
  }
  
  icd_list <- unique(icd_list)
  
  return(icd_list)
  
}


parse_age_label_dt <- function(dt,var_age_label) {
  
  dt_age_label <- data.table(age_label=unique(dt[[var_age_label]]))
  dt_age_label <- dt_age_label[!is.na(age_label) ,]
  dt_age_label[ ,age_list := list()]
  dt_age_label[, temp:=as.numeric(regmatches(age_label, regexpr("[0-9]{1,2}", age_label)))]
  
  setkey(dt_age_label,temp)
  
  
  len <- nrow(dt_age_label)
  for (i in 1:(len-1)) {
    dt_age_label$age_list[[i]] <- dt_age_label$temp[[i]]:(dt_age_label$temp [[i+1]]-1)
  }
  
  dt_age_label$age_list[[len]] <- dt_age_label$temp[[len]]:200
  dt_age_label[ ,temp := NULL]
  
  return(dt_age_label)
  
}


extract_legend_axes<-function(a_gplot){
  pdf(file=NULL)
  tmp <- ggplotGrob(a_gplot)
  leg_index <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box")
  xlab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.x"))
  ylab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.y"))
  title_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.title"))
  subtitle_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.subti"))
  caption_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.capti"))
  
  if(length(leg_index) > 0) {
    legend <- tmp$grobs[[leg_index]]
  }
  
  if(length(subtitle_index) > 0) {
    subtitle <- tmp$grobs[[subtitle_index]]
  }
  
  xlab <- tmp$grobs[[xlab_index]]
  ylab <- tmp$grobs[[ylab_index]]
  title <- tmp$grobs[[title_index]]
  caption <- tmp$grobs[[caption_index]]
  dev.off()
  return(list(legend=legend, xlab=xlab, ylab=ylab, title=title, subtitle=subtitle, caption=caption))
}

reporteRs_OO_patched <- function (docx,temp_path=paste0(tempdir(),"\\temp" )) {
  
  
  # number: 4104, 2382... correspond to the table column wide setup by word and extract from the document.xml..
  unzip(docx, exdir=temp_path)
  tmp_txt <- readLines(paste0(temp_path, "\\word\\document.xml"))
  tmp_txt <- gsub("<w:tblGrid/><w:tr><w:tc>","<w:tblGrid><w:gridCol w:w=\"4104\"/><w:gridCol w:w=\"4104\"/></w:tblGrid><w:tr><w:tc>",tmp_txt,fixed = TRUE)
  tmp_txt <- gsub("<w:tblGrid/><w:tr><w:trPr>","<w:tblGrid><w:gridCol w:w=\"2382\"/><w:gridCol w:w=\"1741\"/><w:gridCol w:w=\"1149\"/><w:gridCol w:w=\"886\"/><w:gridCol w:w=\"825\"/><w:gridCol w:w=\"953\"/><w:gridCol w:w=\"825\"/></w:tblGrid><w:tr><w:trPr>",tmp_txt,fixed = TRUE)
  writeLines(tmp_txt, con=paste0(temp_path, "\\word\\document.xml"))
  
  temp_wd <- getwd()
  setwd(temp_path)
  zip("tmp.docx", files=list.files(all.files = TRUE, recursive = TRUE))
  setwd(temp_wd)
  file.copy(paste0(temp_path,"\\tmp.docx"),docx,overwrite=TRUE)
  #file.remove(temp_path)
  
}

find.java <- function() {
  for (root in c("HLM", "HCU")) for (key in c("Software\\JavaSoft\\Java Runtime Environment", 
                                              "Software\\JavaSoft\\Java Development Kit")) {
    hive <- try(utils::readRegistry(key, root, 2), 
                silent = TRUE)
    if (!inherits(hive, "try-error")) 
      return(hive)
  }
  hive
}
