canreg_error_log <- function(e,filename,out,Args,inc,pop) {
  

  
  #find path and create log file
  pos <- max(gregexpr("\\", out, fixed=TRUE)[[1]])
  path <- substr(out,start=1, stop=pos)
  log_file <- paste0(path, "canreg_log.txt")
  error_connection <- file(log_file,open="wt")
  sink(error_connection)
  sink(error_connection, type="message")
  
  #print error
  cat(paste0("An error occured! please send the log file: `",log_file,"` to  canreg@iarc.fr\n\n"))
  print(paste("MY_ERROR:  ",e))
  cat("\n")
  #print argument from canreg
  print(Args)
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



canreg_load_packages <- function(packages_list) { 
  
  dir.create(file.path(Sys.getenv("R_LIBS_USER")),recursive = TRUE)
  .libPaths(Sys.getenv("R_LIBS_USER"))
  
  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]
  
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 

  new.repos["CRAN"] <- "https://cloud.r-project.org/" #set your favorite  CRAN Mirror here 

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
    
    if ("munsell" %in% installed.packages()[,"Package"]) {
      if (packageVersion("munsell") < "0.2") {
        missing_packages <- c(missing_packages,"munsell" )
      }
    }
  }

  if ("ggplot2" %in% missing_packages) {
    
    if ("gtable" %in% installed.packages()[,"Package"]) {
      if (packageVersion("gtable") < "0.1.1") {
        missing_packages <- c(missing_packages,"gtable" )
      }
    }
    if ("plyr" %in% installed.packages()[,"Package"]) {
      if (packageVersion("plyr") < "1.7.1") {
        missing_packages <- c(missing_packages,"plyr" )
      }
    }
  }

  
  
  if ("ReporteRs" %in% missing_packages) {
     
    if ("rvg" %in% installed.packages()[,"Package"]) {
      if (packageVersion("rvg") < "0.1.2") {
        missing_packages <- c(missing_packages,"rvg")
      }
    }
  }
  



  missing_packages <- unique(missing_packages)

  if(length(missing_packages) > 0 ) {
    for (i in missing_packages) {
      install.packages(i, dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)

    }
  }


    
  lapply(packages_list, require, character.only = TRUE)
  
}




canreg_missing_age <- function(dt,
                               var_age = "AGE_GROUP",
                               var_age_label = "AGE_GROUP_LABEL") {
  
  missing_age <- unique(dt[is.na(get(var_age_label)), c(var_age, var_age_label), with = FALSE])[[var_age]]
  return(missing_age)
  
}

canreg_cancer_info <- function(dt,
                               var_cancer_label = "ICD10GROUPLABEL") {
  
  cancer_label <- substring(as.character(dt[[var_cancer_label]]),4,nchar(as.character(dt[[var_cancer_label]])))
  cancer_sex <- substring(as.character(dt[[var_cancer_label]]),1,3)
  
  return(list(cancer_label = cancer_label, cancer_sex = cancer_sex))
  
}

canreg_import_txt <- function(file,folder) {
  text <- scan(paste0(folder, file), what="character", sep="\n", blank.lines.skip = FALSE, quiet=TRUE)
  for (i in 2:length(text)) {
    text[1] <- paste(text[1], text[i], sep = "\n")
  }
  return(text[1])

}

canreg_report_top_cancer_text <- function(dt_report, percent_equal=5, sex_select="Male") {
  
  dt_temp <- as.data.table(dt_report)
  dt_temp <- dt_temp[SEX==sex_select]
  dt_temp[, cancer_rank:= frank(-CASES, ties.method="first")]
  setkeyv(dt_temp, c("cancer_rank"))
  temp <- dt_temp[cancer_rank==1,CASES]
  dt_temp[, pct_temp:=(temp-CASES)/temp*100]
  dt_temp[pct_temp<=percent_equal, rank:=1]
  
  temp <- dt_temp[cancer_rank==2,CASES]
  dt_temp[, pct_temp:=(temp-CASES)/temp*100]
  dt_temp[pct_temp<=percent_equal & is.na(rank), rank:=2]
  
  dt_temp <- dt_temp[rank<=2]
  
  label1 <- dt_temp[rank==1,cancer_label]
  cases1 <- dt_temp[rank==1,CASES]
  label2 <- dt_temp[rank==2,cancer_label]
  cases2 <- dt_temp[rank==2,CASES]
  
  label1 <- label1[1]
  cases1<- formatC(cases1[1], format="d", big.mark=",")
  
  if (length(label1) > 1) {
    for (i in 2:length(label1)) {
      
      if (i != length(label1)) {
        
        label1 <- paste0(label1,", ",label1[i])
        cases1 <- paste0(cases1,", ",formatC(cases1[i], format="d", big.mark=","))
        
      } else {
        
        label1 <- paste0(label1," and ",label1[i])
        cases1 <- paste0(cases1," and ",formatC(cases1[i], format="d", big.mark=","))
      }
    }
    
    label1 <- paste0(label1," are ")
    
  } else {
    
    label1 <- paste0(label1," is ")
  }
  
  cases1 <- paste0(cases1," cases")
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
  
  
  
  text <-paste0(label1,"the most commonly diagnosed malignancy with ",cases1,
                       " followed by ",text2,".")
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
  dt[, BASIS_pc:=paste0(format(BASIS_pc, big.mark = ",", scientific = FALSE, drop0trailing = TRUE),"%")]
  dt[, CSU_C := NULL]
  
  dt <- reshape(dt, timevar = "BASIS",idvar = c("CSU_label","CSU_ICD","total_cases"), direction = "wide")
  dt[, total_pc_test:= total_cases/sum(total_cases)*200]
  dt[,total_pc_test:=round(total_pc_test,1)]
  dt[, total_pc_test:=paste0(format(total_pc_test, big.mark = ",", scientific = FALSE, drop0trailing = TRUE),"%")]
  setkeyv(dt, c("CSU_ICD"))
  
  if(!("BASIS_pc.0" %in% colnames(dt)))
  {
    dt[, BASIS_pc.0:="0%"]
  }
  
  if(!("BASIS_pc.1" %in% colnames(dt)))
  {
    dt[, BASIS_pc.1:="0%"]
  }
  
  if(!("BASIS_pc.2" %in% colnames(dt)))
  {
    dt[, BASIS_pc.2:="0%"]
  }
  
  setcolorder(dt, c("CSU_label", "CSU_ICD", "total_cases", "total_pc_test", "BASIS_pc.0", "BASIS_pc.1","BASIS_pc.2"))
  
  return(dt)
}




csu_merge_inc_pop <- function(inc_file,
                              pop_file,
                              var_cases = "CASES",
                              var_age = "AGE_GROUP",
                              var_pop = "COUNT",
                              var_ref_count = "REFERENCE_COUNT",
                              var_by = NULL,
                              column_group_list = NULL){
  
  df_inc <- read.table(inc_file, header=TRUE)
  df_pop <- read.table(pop_file, header=TRUE)
  
  dt_inc <- data.table(df_inc)
  dt_pop <- data.table(df_pop)
  
  setnames(dt_inc, var_cases, "CSU_C")
  
  dt_inc <- dt_inc[, c(var_age, var_by, "CSU_C"), with = FALSE]
  dt_inc <-  dt_inc[,list(CSU_C = sum(CSU_C)), by=eval(colnames(dt_inc)[!colnames(dt_inc) %in% c("CSU_C")])]
  
  if (!is.null(column_group_list)){
    cj_var <- colnames(dt_inc)[!colnames(dt_inc) %in% c("CSU_C",lapply(column_group_list, `[[`, 2))]
  } else {
    cj_var <-colnames(dt_inc)[!dt_inc %in% c("CSU_C")]
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
  
  dt_pop <- dt_pop[get(var_pop) != 0,]
  dt_pop[[var_ref_count]] <-  dt_pop[[var_ref_count]]*100
  intersect(colnames(dt_inc),colnames(dt_pop))
  
  dt_all <- merge(dt_inc, dt_pop,by=intersect(colnames(dt_inc),colnames(dt_pop)), all.x=TRUE)
  setnames(dt_all,"CSU_C",var_cases)
  return(dt_all)
}


canreg_ageSpecific_rate_data <- function(dt, keep_ref=FALSE, keep_year=FALSE, keep_basis = FALSE) { 
  
  var_by <- c("ICD10GROUP", "ICD10GROUPLABEL", "AGE_GROUP","AGE_GROUP_LABEL", "SEX")
  if (keep_ref) {
    var_by <- c(var_by, "REFERENCE_COUNT")
  }
  
  if (keep_year) {
    var_by <- c(var_by, "YEAR")
  }
  
  if (keep_basis) {
    var_by <- c(var_by, "BASIS")
  }
  
  dt <-  dt[AGE_GROUP != canreg_missing_age(dt) ,list(CASES=sum(CASES), COUNT=sum(COUNT)), by=var_by]
  dt$cancer_label <- canreg_cancer_info(dt)$cancer_label
  dt$cancer_sex <- canreg_cancer_info(dt)$cancer_sex
  dt$cancer_title <- paste(dt$cancer_label, "\n(", dt$ICD10GROUP, ")", sep="")
  dt$SEX <- factor(dt$SEX, levels=c(1,2), labels=c("Male", "Female"))
  dt <- dt[, -c("ICD10GROUPLABEL"), with=FALSE]
  dt <- dt[!((substring(cancer_sex, 1, 1) ==0) & SEX == "Male"),]
  dt <- dt[!((substring(cancer_sex, 2, 2) ==0) & SEX == "Female"),]
  
  
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


canreg_get_agegroup_label <- function(dt, first_age, last_age) {
  
  temp_max <- max(dt$AGE_GROUP)
  temp_min <- min(dt$AGE_GROUP)
  if (temp_max < last_age) {
    last_age = temp_max
  } 
  if (temp_min > first_age) {
    temp_min = first_age
  } 
  temp1 <- as.character(unique(dt[dt$AGE_GROUP == first_age,]$AGE_GROUP_LABEL))
  temp2 <-as.character(unique(dt[dt$AGE_GROUP == last_age,]$AGE_GROUP_LABEL))
  temp1 <- substr(temp1,1,regexpr("-", temp1)[1]-1)
  temp2 <- substr(temp2,regexpr("-", temp2)[1]+1,nchar(temp2))
  return(paste0(temp1,"-",temp2, " years"))
}

csu_asr_core <- function(df_data, var_age, var_cases, var_py, var_by=NULL,
                         var_age_group=NULL, missing_age = NULL, var_st_err=NULL,
                         first_age = 1, last_age = 18,db_rate = 100000, pop_base = "SEGI",
                         correction_info=FALSE, var_asr="asr", age_dropped = FALSE,
                         pop_base_count = NULL, age_label_list = NULL) {
  
  
  
  bool_dum_by <- FALSE
  bool_dum_age <- FALSE
  
  if (first_age < 1 | first_age > 17 ) {
    stop('The argument "first_age" must be comprise between 1 (0-4) and 17 (80-85), see documentation: help(csu_asr)')
  }
  
  if (last_age < 2 | last_age > 18 ) {
    stop('The argument "last_age" must be comprise between 2 (5-9) and 18 (85+), see documentation: help(csu_asr)')
  }
  
  if (!(var_age%in% colnames(df_data))) {
    
    stop('var_age value is not a variable name of the data, see documentation: Help(csu_asr)')
    
  }
  
  if (!(var_cases%in% colnames(df_data))) {
    
    stop('var_cases value is not a variable name of the data, see documentation: Help(csu_asr)')
    
  }
  
  if (!(var_py%in% colnames(df_data))) {
    
    stop('var_py value is not a variable name of the datae, see documentation: Help(csu_asr)')
    
  }
  
  
  
  if (is.null(var_by)) {
    
    df_data$CSU_dum_by <- "dummy_by"
    var_by <- "CSU_dum_by"
    bool_dum_by <- TRUE
    
  }
  
  
  if (is.null(var_age_group)) {
    
    df_data$CSU_dum_age <- "dummy_age_gr"
    var_age_group <- "CSU_dum_age"
    var_by <- c(var_by, "CSU_dum_age")
    bool_dum_age <- TRUE
    
  }
  
  
  dt_data <- data.table(df_data, key = var_by) 
  setnames(dt_data, var_age, "CSU_A")
  setnames(dt_data, var_cases, "CSU_C")
  setnames(dt_data, var_py, "CSU_P")
  
  temp <- dt_data[, lapply(.SD, function(x) is.numeric(x)) ]
  
  if (!temp[["CSU_A"]]) {
    
    stop('The variable "age" must be numeric, see documentation: help(csu_asr)')
    
  }
  
  if (!temp[["CSU_P"]]) {
    
    stop('The variable "population" must be numeric, see documentation: help(csu_asr)')
    
  }
  
  if (!temp[["CSU_C"]]) {
    
    stop('The variable "age" must be numeric, see documentation: help(csu_asr)')
    
  }
  
  temp <- NULL
  
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
    
    
    dt_data[, total:=sum(CSU_C), by=var_by] #add total
    dt_data[!is.na(dt_data$age_factor) , total_known:=sum(CSU_C), by=var_by] #add total_know
    dt_data$correction <- dt_data$total / dt_data$total_know 
    dt_data[is.na(dt_data$correction),correction:=1 ] 
    dt_data$total <- NULL
    dt_data$total_known <- NULL
    
  }
  
  if (is.null(pop_base_count)) {
    
    # create world population DF for different nb of age group
    SEGI_pop <- c(12000,10000,9000,9000,8000,8000,6000,6000,6000,6000,5000,4000,4000,3000,2000,1000,500,500)
    EURO_pop <- c(8000,7000,7000,7000,7000,7000,7000,7000,7000,7000,7000,6000,5000,4000,3000,2000,1000,1000)
    
    if (pop_base == "EURO") {
      pop <- EURO_pop
    } else {
      pop <- SEGI_pop
    }
    
    # calculated total pop for age selected 
    total_pop <- sum(pop[first_age:last_age])
    
    Standard_pop <- data.table(pop = pop, age_factor= c(1:18))
    
    pop[17] <- pop[17]+ pop[18]
    pop[18] <- 0
    Standard_pop$pop17 <- pop
    pop[16] <- pop[16]+ pop[17]
    pop[17] <- 0
    Standard_pop$pop16 <- pop
    pop[15] <- pop[15]+ pop[16]
    pop[16] <- 0
    Standard_pop$pop15 <- pop
    
    #age dropped option
    if (age_dropped) {
      dt_data$age_factor <- dt_data$age_factor + first_age -1   
    }
    
    
    # keep age selected 
    dt_data=dt_data[dt_data$age_factor %in% c(first_age:last_age) | is.na(dt_data$age_factor), ]
    
    # calculated maximum age group with population data
    if (last_age == 18) {
      dt_data <- merge(dt_data, dt_data[dt_data$CSU_P != 0,list(nb_age_group = max(age_factor)), by=var_age_group], by=var_age_group)  
    } else {
      dt_data$nb_age_group <- 18
    }
    
    # show population with less than 18 age group
    if (last_age == 18) {
      temp <- subset(dt_data,nb_age_group <18, select= c(var_age_group, "nb_age_group"))
      if (nrow(temp) >0) {
        setkey(temp,NULL)
        #cat("\n")
        #cat("Population with less than 18 age group:\n\n" )
        #print
        #print(unique(temp), row.names = FALSE)
        #cat("\n")
      }
      temp <- NULL
    }
    
    #regroup case for population with nb of age group <  18 
    for (i in 15:17) {
      
      if (i %in% dt_data$nb_age_group) {
        
        dt_data[nb_age_group == i & age_factor >= i , CSU_C:=sum(CSU_C), by=var_by] #add total_know
        dt_data[nb_age_group == i & age_factor > i & !is.na(age_factor), CSU_C := 0] 
        
      } 
    }
    
    #add world pop to database 
    dt_data <- merge(dt_data,Standard_pop, by =c("age_factor"), all.x=TRUE )
    Standard_pop <- NULL
    dt_data[nb_age_group==17, pop:=pop17[dt_data$nb_age_group==17]]
    dt_data[nb_age_group==16, pop:=pop16[dt_data$nb_age_group==16]]
    dt_data[nb_age_group==15, pop:=pop15[dt_data$nb_age_group==15]]
    
  } else {
    
    #keep age group selected 
    dt_data <- dt_data[age_factor %in% (first_age:last_age), ]
    
    #calcul total pop for canreg
    total_pop <-sum(unique(dt_data[, c("age_factor", pop_base_count), with=FALSE])[[pop_base_count]])
    
    #get age group list variable
    if (is.null(age_label_list)) {
      age_label_list <- var_age
    }
    age_group_list <- as.character(unique(dt_data[[age_label_list]]))
    age_group_list <- paste(age_group_list,  collapse=" ")
    
    #rename variable population reference
    setnames(dt_data, pop_base_count, "pop")
  }
  
  #calcul ASR
  
  dt_data[dt_data$CSU_P != 0,rate:= dt_data$CSU_C[dt_data$CSU_P != 0]/ dt_data$CSU_P[dt_data$CSU_P != 0] * db_rate]
  dt_data$asr <- dt_data$rate * dt_data$pop
  dt_data[is.na(dt_data$asr),asr:=0 ] 
  
  dt_data$st_err <- ( dt_data$rate * (dt_data$pop^2) * (db_rate - dt_data$rate))/dt_data$CSU_P
  dt_data[is.na(dt_data$st_err),st_err:=0 ] 
  
  # to check order 
  dt_data<- dt_data[order(dt_data$index_order ),]
  dt_data<-  dt_data[,list( CSU_C=sum(CSU_C), CSU_P=sum(CSU_P),asr=sum(asr),st_err = sum(st_err),correction = max(correction)), by=var_by]
  
  dt_data$asr <- dt_data$asr / total_pop
  dt_data$asr <- dt_data$asr * dt_data$correction
  dt_data$st_err <- (dt_data$st_err / (total_pop^2))^(1/2)
  dt_data$st_err <- dt_data$st_err * dt_data$correction
  
  dt_data$asr <- round(dt_data$asr, digits = 2)
  dt_data$st_err <- round(dt_data$st_err, digits = 2)
  dt_data$correction <- round((dt_data$correction-1)*100, digits = 1)
  
  if (is.null(var_st_err)) {
    dt_data$st_err <- NULL
  } else {
    setnames(dt_data, "st_err", var_st_err)
  }
  
  if (var_asr!="asr") {
    setnames(dt_data, "asr", var_asr)
  }
  
  if (!correction_info) {
    dt_data$correction <- NULL
  }
  
  
  df_data <- data.frame(dt_data)
  
  
  
  if (bool_dum_age) {
    df_data$CSU_dum_age <- NULL
  }
  if (bool_dum_by) {
    df_data$CSU_dum_by <- NULL
  }
  
  setnames(df_data, "CSU_C", var_cases)
  setnames(df_data,  "CSU_P", var_py)
  
  
  if (is.null(pop_base_count)) {
    
    temp <- last_age*5-1
    if (last_age == 18)  temp <- "99+"
    #cat("ASR have been computed for the age group ", (first_age-1)*5,"-", temp , "\n",  sep="" )
    temp<- NULL
    
  } else {
    
    #cat("ASR have been computed for the age groups:\n",age_group_list , "\n",  sep="" )
    age_group_list<- NULL
    
  }
  
  return(df_data)
  
}

csu_asr_new <-
  function(df_data,var_age="age", var_cases="cases", var_py="py", var_by=NULL, 
           var_age_group=NULL, missing_age = NULL, var_st_err=NULL,
           first_age = 1, last_age = 18, db_rate = 100000, pop_base = "SEGI",
           correction_info=FALSE, var_asr="asr", age_dropped = FALSE) {
    
    
    df_data <- csu_asr_core(df_data,var_age,var_cases,var_py,var_by,var_age_group,missing_age,var_st_err,
                            first_age,last_age,db_rate,pop_base,correction_info,var_asr,age_dropped)
    
    
    return(df_data)
    
  }


csu_cum_risk_core <- function(df_data, var_age, var_cases, var_py, group_by=NULL,
                              missing_age = NULL,last_age = 15,
                              var_cum_risk="cum_risk",
                              age_label_list = "AGE_GROUP_LABEL") {
  
  
  
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
    
  }
  

  
  # calcul year interval from age group label
  
  dt_temp <- unique(dt_data[, c(age_label_list), with=FALSE])
  dt_temp[, min:=as.numeric(regmatches(get(age_label_list), regexpr("[0-9]+",get(age_label_list))))]
  dt_temp[, max:=shift(min, type ="lead")]
  dt_temp[, age_span := max-min]
  dt_temp <- dt_temp[, c("age_span",age_label_list), with=FALSE]
  dt_data <- merge(dt_data, dt_temp,by= age_label_list, all.x=TRUE)
  
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

csu_ageSpecific_core <-
  function(df_data,var_age="age",
           var_cases="cases",
           var_py="py",
           var_by = NULL,
           missing_age = NULL,
           db_rate = 100000,
           log_scale=FALSE,
           plot_title=NULL,
           plot_subtitle=NULL,
           plot_caption=NULL,
           legend=csu_trend_legend(),
           CI5_comparaison=NULL,
           color_trend = NULL,
           age_label_list = NULL) {
    

    
    
    if (!(legend$position %in% c("bottom", "right"))) {
      
      stop('legend position must be "bottom" or "right", see documentation: Help(csu_ageSpecific)')
      
    }
    
    if (legend$right_space_margin > 20) {
      
      stop('legend right space margin must be < 20, see documentation: Help(csu_ageSpecific)')
      
    }
    
    
    if (!(var_age%in% colnames(df_data))) {
      
      stop('var_age value is not a variable name of the data, see documentation: Help(csu_ageSpecific)')
      
    }
    
    if (!(var_cases%in% colnames(df_data))) {
      
      stop('var_cases value is not a variable name of the data, see documentation: Help(csu_ageSpecific)')
      
    }
    
    if (!(var_py%in% colnames(df_data))) {
      
      stop('var_py value is not a variable name of the data, see documentation: Help(csu_ageSpecific)')
      
    }
    
    bool_CI5_comp <- FALSE
    CI5_cancer_label <- NULL
    
    if (!is.null(CI5_comparaison)) {
      
      bool_CI5_comp <- TRUE
      data(csu_ci5x_mean, envir = e <- new.env())
      df_CI5 <- e$csu_ci5x_mean
      dt_CI5 <- data.table(df_CI5)
      if (is.character(CI5_comparaison)) {
        if (!(CI5_comparaison%in% dt_CI5$ci5_cancer_label)) {
          stop('CI5_comparaison value must be a correct cancer label, see documentation: Help(CI5X_mean_data)')
          
        } else {
          dt_CI5 <- dt_CI5[dt_CI5$ci5_cancer_label == CI5_comparaison, ]
        }
        
      } else {
        if (is.numeric(CI5_comparaison)) {
          if (!(CI5_comparaison%in% dt_CI5$ci5_cancer_code)) {
            stop('CI5_comparaison value must be a correct cancer code, see documentation: Help(CI5X_mean_data)')
            
          } else {
            dt_CI5 <- dt_CI5[dt_CI5$ci5_cancer_code == CI5_comparaison, ]
          }
        }
      }
      CI5_cancer_label <- toString(dt_CI5$ci5_cancer_label[1])
    }
    
    bool_dum_by <- FALSE
    
    if (is.null(var_by)) {
      
      df_data$CSU_dum_by <- "dummy_by"
      var_by <- "CSU_dum_by"
      bool_dum_by <- TRUE
    }
    
    
    if ( length(var_by) > 1) {
      
      stop('Only one variable can be use in the "var_by" option, see documentation: Help(csu_ageSpecific)')
      
    }
    
    if (!(var_by%in% colnames(df_data))) {
      
      stop('var_by value is not a variable name of the data, see documentation: Help(csu_ageSpecific)')
      
    }
    
    dt_data <- data.table(df_data, key = var_by)
    setnames(dt_data, var_age, "CSU_A")
    setnames(dt_data, var_cases, "CSU_C")
    setnames(dt_data, var_py, "CSU_P")
    setnames(dt_data, var_by, "CSU_BY")
    
    temp <- dt_data[, lapply(.SD, function(x) is.numeric(x)) ]
    
    if (!temp[["CSU_A"]]) {
      
      stop('The variable "age" must be numeric, see documentation:  Help(csu_ageSpecific)')
      
    }
    
    if (!temp[["CSU_P"]]) {
      
      stop('The variable "population" must be numeric, see documentation:  Help(csu_ageSpecific)')
      
    }
    
    if (!temp[["CSU_C"]]) {
      
      stop('The variable "age" must be numeric, see documentation:  Help(csu_ageSpecific)')
      
    }
    
    if (!is.null(missing_age)) {
      if (!(missing_age %in% dt_data$CSU_A)) {
        stop('missing_age is not in the age value, see documentation: Help(csu_ageSpecific)')
      }
    }
    
    ##group population (use sum)
    dt_data <- dt_data[, list(CSU_C=sum(CSU_C),CSU_P=sum(CSU_P)), by=c("CSU_BY", "CSU_A") ]
    
    ##calcul rate 
    dt_data$rate <- dt_data$CSU_C/dt_data$CSU_P *db_rate
    
    ##change by to factor
    dt_data$CSU_BY <- factor(dt_data$CSU_BY)
    
    ##to calcul age group
    
    dt_data[CSU_A==missing_age,CSU_A:=NA ] 
    dt_data[is.na(CSU_A),CSU_P:=0 ] 
    dt_data <- dt_data[CSU_P!=0] 
    
    dt_data$CSU_age_factor <- c(as.factor(dt_data$CSU_A))
    dt_data[CSU_P != 0,nb_age_group := max(CSU_age_factor), by="CSU_BY"] 
    max_age <- max(dt_data$nb_age_group)
    
    for (i in 15:17) {
      if (i %in% dt_data$nb_age_group) {
        dt_data[nb_age_group == i & CSU_age_factor >= i , CSU_C:=sum(CSU_C), by="CSU_BY"] ##add total_know
        dt_data[nb_age_group == i & CSU_age_factor > i & !is.na(CSU_age_factor), CSU_C := 0] 
      } 
    }
    ##create age label:
    if (is.null(age_label_list)) {
      
      
      ##create age dummy: 1 2 3 4 --- 18
      
      
      
      ##regroup case for population with nb of age group <  18 
      
      
      
      age_label <- c("0-4","5-9","10-14","15-19","20-24","25-39","30-34","35-39","40-44", "45-49","50-54","55-59","60-64","65-69","70-74","75-79","80-84","85+")
      
      if (max_age  < 18 ) {
        age_label <- c(age_label[1:16],"80+") 
        if (max_age  < 17) {
          age_label <- c(age_label[1:15],"75+") 
          if (max_age  < 16) {
            age_label <- c(age_label[1:14],"70+") 
            if (max_age  == 15) {
              age_label <- c(age_label[1:14],"65+") 
            } else {
              stop('The data need at least 15 age-group, see documentation: Help(csu_graph_ageSpecific)')
            }
          }
        }
      } else {
        if (max_age > 18) {
          stop('The function cannot have more than 18 age-group, see documentation: Help(csu_graph_ageSpecific)')
        }
      }
    } else {
      age_label <-age_label_list
      max_age <- length(age_label)
    }
    
    
    
    
    ## to calcul breaks
    tick <- csu_tick_generator(max = max(dt_data$rate), min=min(dt_data[rate != 0,]$rate), log_scale = log_scale )


    
    tick_space <- tick$tick_list[length(tick$tick_list)] - tick$tick_list[length(tick$tick_list)-1]
    

    temp_top <- ceiling(max(dt_data$rate)/tick_space)*tick_space
    temp_expand_y <- max(dt_data$rate)/35
    temp_expand_y_up <- max(dt_data$rate)+temp_expand_y
    if (temp_expand_y_up > temp_top-(tick_space/2)) {
      temp_expand_y_up <- temp_top+temp_expand_y
    }
    
    th_legend <- list(theme(legend.position="none"))
    
    if (!bool_dum_by & legend$position == "bottom") {
      
      th_legend <- list(theme(
        legend.key = element_rect(fill="transparent"),
        legend.position = "bottom",
        legend.text = element_text(size = 14),
        legend.title = element_text(size = 14),
        legend.key.size=unit(1,"cm"),
        legend.margin = margin(0, 0, 0, 0)
      ))
    }
    
    if (bool_CI5_comp & is.null(age_label_list)) {
      
      if (max_age < 18) {
        dt_CI5[CSU_age_factor >= max_age , CSU_C:=sum(CSU_C)] ##add total_know
        dt_CI5[ CSU_age_factor >= max_age , CSU_P:=sum(CSU_P)]
        dt_CI5 <- dt_CI5[CSU_age_factor <= max_age]    
      }
      
      dt_CI5$rate <- dt_CI5$CSU_C/dt_CI5$CSU_P *db_rate
      
    }
    
    
    
    ##csu_plot
    
    
    
    if (log_scale) {
      base_plot <- ggplot(dt_data[, rate := ifelse(rate==0,NA, rate )], aes(CSU_age_factor, rate))
    } else {
      base_plot <- ggplot(dt_data, aes(CSU_age_factor, rate))
    }
    if (bool_CI5_comp) {
      
      pos_y_text = - tick_space
      if (temp_top/tick_space > 7) {
        
        pos_y_text = pos_y_text*1.5
        
      }
      
      str_CI5 <- textGrob("- - - - - - : CI5 X", gp=gpar(fontsize=11, col = "grey30"))
      
      base_plot <- base_plot + 
        geom_line(data = dt_CI5,
                  size = 1,
                  linetype=2,
                  colour = "grey50", 
                  show.legend=FALSE)##+
      ##annotation_custom(str_CI5,xmin=max_age-2,xmax=max_age-2,ymin=pos_y_text,ymax=pos_y_text)
      
    } 
    
    
    csu_plot <- base_plot+
      geom_line(aes(color=CSU_BY), size = 1,na.rm=TRUE)+
      guides(color = guide_legend(override.aes = list(size=0.75)))+
      labs(title = plot_title,
           subtitle = plot_subtitle,
           caption = plot_caption)+
      scale_x_continuous(name = "Age at diagnosis",
                         breaks=seq(1, max_age, 1),
                         labels = age_label,
                         minor_breaks = NULL,
                         expand = c(0.015,0.015)
      )
    
    if (log_scale){
      

      csu_plot <- csu_plot +
        #geom_point(aes(fill=CSU_BY), size = 3,na.rm=TRUE,shape=21,stroke=0.5,colour="black", show.legend=FALSE)+
        scale_y_continuous(name = paste("Age-specific incidence rate per", formatC(db_rate, format="d", big.mark=",")),
                           breaks=tick$tick_list,
                           minor_breaks = tick$tick_minor_list,
                           limits=c(tick$tick_list[1],tick$tick_list[length(tick$tick_list)]),
                           labels=csu_axes_label,
                           trans = "log10"
        )
    } else {
      
      csu_plot <- csu_plot +
        coord_cartesian( ylim=c(-temp_expand_y, temp_expand_y_up),  expand = TRUE)+
        scale_y_continuous(name = paste("Age-specific incidence rate per", formatC(db_rate, format="d", big.mark=",")),
                           breaks=tick$tick_list,
                           labels=csu_axes_label,
                           expand = c(0,0)
        )
    } 
    

    
    csu_plot <- csu_plot +
      theme(
        plot.background= element_blank(),
        panel.background = element_blank(),
        panel.grid.major= element_line(colour = "grey70"),
        panel.grid.minor= element_line(colour = "grey70"),
        plot.title = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
        plot.subtitle = element_text(size=15, margin=margin(0,0,15,0),hjust = 0.5),
        plot.caption = element_text(size=10, margin=margin(15,0,0,0)),
        axis.title = element_text(size=14),
        axis.title.y = element_text(margin=margin(0,15,0,0)),
        axis.title.x = element_text(margin=margin(15,0,0,0)),
        plot.margin=margin(20,20,20,20),
        axis.text = element_text(size=14, colour = "black"),
        axis.text.x = element_text(size=14, angle = 60,  hjust = 1),
        axis.ticks= element_line(colour = "black", size = 0.5),
        axis.ticks.length = unit(0.2, "cm"),
        axis.line.x = element_line(colour = "black", 
                                   size = 0.5, linetype = "solid"),
        axis.line.y = element_line(colour = "black", 
                                   size = 0.5, linetype = "solid")
      )+
      th_legend
    
    
    if (!is.null(color_trend)) {
      
      csu_plot <- csu_plot +
        scale_colour_manual(name=legend$title,
                            values= color_trend,
                            drop = FALSE)
      
      if (log_scale) {
        csu_plot <- csu_plot +
          scale_fill_manual(values= color_trend,
                            drop = FALSE)
      }
      
      
    } else {
      csu_plot <- csu_plot +
        scale_colour_discrete(name=legend$title)
    }
    
    if (!bool_dum_by & legend$position=="right") {

      csu_plot <- csu_plot + 
        geom_text(data = dt_data[CSU_age_factor == nb_age_group, ],
                  aes(label = CSU_BY),
                  hjust=-0.05)+
        theme(plot.margin = unit(c(0.5, legend$right_space_margin, 0.5, 0.5), "lines"))
      
    } else {
     
      csu_plot <- csu_plot +
        guides(color = guide_legend(nrow=legend$nrow))
    }


    
    dt_data$nb_age_group <- NULL
    dt_data$CSU_age_factor <- NULL
    
    if (log_scale){
      dt_data[, rate := ifelse(is.na(rate),0, rate )]
    }
    
    
    return(list(csu_plot = csu_plot, dt_data = dt_data, CI5_cancer_label = CI5_cancer_label,legend_position=legend$position, bool_dum_by = bool_dum_by))
    
  }




csu_ageSpecific_new <- 
  function(df_data,var_age="age", var_cases="cases", var_py="py", var_by = NULL, missing_age = NULL,db_rate = 100000,  plot_title=NULL, legend=csu_trend_legend(),log_scale=FALSE, CI5_comparaison=NULL, format_export=NULL, draw_plot=TRUE,var_rate="rate") {
    
    csu_list <- csu_ageSpecific_core(df_data,var_age, var_cases, var_py, var_by , missing_age,db_rate,log_scale = log_scale,  plot_title, legend, CI5_comparaison)
    dt_data <- csu_list$dt_data
    
    ##format
    if (!is.null(format_export)) {
      if (format_export == "pdf") {
        
        pdf(paste(plot_title,".pdf", sep=""))
        
      } else {
        if (format_export == "svg") {
          svg(paste(plot_title,".svg", sep=""))
        }
      }
    }
    
    if (draw_plot) {
      if (csu_list$legend_position=="right") {
        gb_plot <- ggplot_build(csu_list$csu_plot)
        gt_plot <- ggplot_gtable(gb_plot)
        gt_plot$layout$clip[gt_plot$layout$name=="panel"] <- "off"
        if(is.null(format_export)) {
          plot.new()
        }
        grid.draw(gt_plot)
      } else {
        print(csu_list$csu_plot)
      }
    }
    
    if (!is.null(csu_list$CI5_cancer_label)) {
      cat("the dotted grey line represente the mean for ", csu_list$CI5_cancer_label, " cancer in CI5 X\n", sep="")
      
    }
    
    if (!is.null(format_export)) {
      dev.off()
    }
    
    
    if (var_rate!="rate") {
      setnames(dt_data, "rate", var_rate)
    }
    
    setorder(dt_data,CSU_BY,CSU_A)
    
    df_data <- data.frame(dt_data)
    
    setnames(df_data, "CSU_A", var_age)
    setnames(df_data, "CSU_C", var_cases)
    setnames(df_data,  "CSU_P", var_py)
    if (!csu_list$bool_dum_by) {
      setnames(df_data,  "CSU_BY", var_by)
    } else {
      
      df_data$CSU_BY <- NULL
    }
    
    return(df_data)
    
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
                                               var_by="SEX",
                                               var_age_label_list = "AGE_GROUP_LABEL",
                                               color_trend=c("Male" = "#08519c", "Female" = "#a50f15"),
                                               log_scale=FALSE,
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
  
  if (log_scale){
    
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
    temp <- csu_ageSpecific_core(dt_temp,
                                 var_age=var_age,
                                 var_cases= var_cases,
                                 var_py=var_py,
                                 var_by = var_by,
                                 plot_title = canreg_header,
                                 plot_subtitle = cancer_title,
                                 plot_caption = canreg_header,
                                 color_trend = color_trend,
                                 log_scale = log_scale,
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
      
      
      if (log_scale) {
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
      
      if (log_scale) {
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
                                            canreg_header=NULL,
                                            canreg_report = FALSE) {
  
  
  
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
                                 canreg_header = canreg_header,
                                 canreg_report = canreg_report
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


  
  if(!canreg_report) {
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
    
  } else {
    return(
      grid.arrange(
      grobs=plotlist_grid,
      layout_matrix = lay,
      widths = widths,
      heights=heights,
      left=" ",
      top= " ",
      bottom= " ",
      right= " "
    ))
  }
  
  
}

canreg_ageSpecific_rate_top <- function(dt, var_age="AGE_GROUP", 
                                        var_cases= "CASES", 
                                        var_py= "COUNT",
                                        var_by="SEX",
                                        var_age_label_list = "AGE_GROUP_LABEL",
                                        log_scale = TRUE,
                                        nb_top = 5,
                                        landscape = FALSE,
                                        list_graph = FALSE,
                                        return_data = FALSE,
                                        canreg_header="",
                                        canreg_report=FALSE) {
  
  
  
  dt <- csu_dt_rank(dt, var_value = var_cases, var_rank = "cancer_label",group_by = "SEX", number = nb_top) 
  

  
  
  
  
  if (return_data) {
    dt[, rate := CASES/COUNT*10000]
    dt[, cancer_sex := NULL]
    dt[, cancer_title := NULL]
    dt[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
    setnames(dt, "CSU_RANK","cancer_rank")
    dt <- dt[, c("cancer_label",
                 "ICD10GROUP",
                 "cancer_rank",
                 "SEX",
                 "AGE_GROUP",
                 "AGE_GROUP_LABEL",
                 "CASES",
                 "COUNT",
                 "rate"), with=FALSE]
    setkeyv(dt, c("SEX","cancer_rank","ICD10GROUP" ,"AGE_GROUP" ))
    return(dt)
    stop() 
  }
  
  dt$cancer_label <-csu_legend_wrapper(dt$cancer_label, 14)
  
  
  plotlist <- list()
  j <- 1 
  for (i in levels(dt[[var_by]])) {
    
    if (j == 1) {
      plot_title <- canreg_header
      plot_caption <- ""
    } else {
      plot_title <- ""
      plot_caption <- canreg_header
    }
      
    

    
    dt_plot <- dt[get(var_by) == i]
    dt_label_order <- setkey(unique(dt_plot[, c("cancer_label", "CSU_RANK"), with=FALSE]), CSU_RANK)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = dt_label_order$cancer_label) 
    
    color_cancer <- csu_cancer_color(cancer_list =dt_label_order$cancer_label)

    
    plotlist[[j]] <- csu_ageSpecific_core(dt_plot,
                                          var_age=var_age,
                                          var_cases= var_cases,
                                          var_py=var_py,
                                          var_by = "cancer_label",
                                          plot_title = plot_title,
                                          plot_subtitle = paste0("Top ",nb_top, " cancer sites\n",i),
                                          plot_caption = plot_caption,
                                          color_trend = color_cancer,
                                          log_scale = log_scale,
                                          age_label_list = unique(dt_plot[[var_age_label_list]]),
    )$csu_plot
    
    j <- j+1
  }
  
  
  if(!canreg_report) {
    print(plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))
    print(plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))
  } else {
    return(list(male=plotlist[[1]], female=plotlist[[2]]))
  }
  
}


canreg_bar_top_single <- function(dt, var_top, var_bar = "cancer_label" ,group_by = "SEX",
                                  nb_top = 10, landscape = FALSE,list_graph=TRUE,
                                  canreg_header = "", xtitle = "",digit  =  1,
                                  return_data  =  FALSE) {
  
  dt <- csu_dt_rank(dt, var_value = var_top, var_rank = var_bar,group_by = group_by, number = nb_top) 
  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    setkeyv(dt, c("SEX","cancer_rank"))
    return(dt)
    stop() 
  }
  
  dt$cancer_label <-csu_legend_wrapper(dt$cancer_label, 15)
  
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
    dt_label_order <- setkey(unique(dt_plot[, c(var_bar, "CSU_RANK"), with=FALSE]), CSU_RANK)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = rev(dt_label_order$cancer_label)) 
    color_cancer <- csu_cancer_color(cancer_list =rev(dt_label_order$cancer_label))
    


    
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


csu_bar_plot <- function(dt, 
                             var_top,
                             var_bar,
                             plot_title = plot_title,
                             plot_subtitle = "", 
                             plot_caption = NULL,
                             xtitle="",
                             digit = 1,
                             color_bar = NULL,
                             landscape = FALSE)  {
  
  line_size <- 0.4
  text_size <- 14 
  
  if (landscape) {
    csu_ratio = 0.6
    csu_bar_label_size = 4
  } else {
    csu_ratio = 1.4
    csu_bar_label_size = 5
  }
  
  dt[, plot_value:= get(var_top)]
  
  
  tick_major_list <- csu_tick_generator(max = max(dt$plot_value), 0)$tick_list
  nb_tick <- length(tick_major_list) 
  tick_space <- tick_major_list[nb_tick] - tick_major_list[nb_tick-1]
  if ((tick_major_list[nb_tick] -  max(dt$plot_value))/tick_space < 1/4){
    tick_major_list[nb_tick+1] <- tick_major_list[nb_tick] + tick_space
  }
  

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
                       labels=csu_axes_label
                       
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
      legend.position = "none",
    )
  
  return(csu_plot)
  
}


canreg_bar_top <- function(df_data,
                               var_top = "asr",
                               var_bar = "cancer_label",
                               var_by = "SEX",
                               nb_top = 10,
                               color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                               landscape = FALSE,
                               list_graph = FALSE,
                               canreg_header=NULL,
                               ytitle = "",
                               nsmall = 1,
                               return_data = FALSE,
                               plot_caption= NULL,
                               canreg_report=FALSE) {
  
  
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
  setnames(dt, var_by, "CSU_BY")
  
  plot_subtitle <- paste("top",nb_top,"cancer sites")
  
  dt <- csu_dt_rank(dt, var_value = "CSU_ASR", var_rank = "CSU_BAR",number = nb_top)
  
  
  
  if (return_data) {
    dt[, rank_value := NULL]
    setnames(dt, "CSU_BAR",var_bar)
    setnames(dt, "CSU_BY", var_by)
    setnames(dt, "CSU_ASR", var_top)
    setnames(dt, "CSU_RANK","cancer_rank")
    setkeyv(dt, c("cancer_rank",var_bar))
    return(dt)
    stop() 
  }
  
  
  
  if (!canreg_report) dt$CSU_BAR <-csu_legend_wrapper(dt$CSU_BAR, 15)
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], asr_plot:= CSU_ASR*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], asr_plot:= CSU_ASR]
  
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  factor_order <- unique(dt[, c("CSU_BAR", "CSU_RANK"), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = rev(setkeyv(factor_order, "CSU_RANK")$CSU_BAR)) 
  
  tick_minor_list <- csu_tick_generator(max = max(dt$CSU_ASR), 0)$tick_list
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
  
  if(!canreg_report){
    print(csu_plot)
  } 
  else {
    return(csu_plot)
  }
  
  
}

canreg_population_pyramid <- function(df_data,
                                      var_cases = "Percent",
                                      var_bar = "AGE_GROUP_LABEL",
                                      var_by = "SEX",
                                      var_age_cut="AGE_GROUP",
                                      color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                                      landscape = FALSE,
                                      list_graph = FALSE,
                                      canreg_header=NULL,
                                      return_data = FALSE,
                                      plot_caption= NULL,
                                      canreg_report=FALSE) {
  
  
  
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
  setnames(dt, var_by, "CSU_BY")
  
  dt$CSU_BY <- factor(dt$CSU_BY)
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], cases_plot:= CSU_CASES*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], cases_plot:= CSU_CASES]
  
  factor_order <- unique(dt[, c("CSU_BAR", var_age_cut), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = setkeyv(factor_order, var_age_cut)$CSU_BAR) 
  
  tick_minor_list <- csu_tick_generator(max = max(dt$CSU_CASES), 0)$tick_list
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
  

if(!canreg_report){
  print(csu_plot)
} 
else {
  return(csu_plot)
}



}
canreg_cases_age_bar <- function(df_data,
                                     var_cases = "CASES",
                                     var_bar = "group_label",
                                     var_by = "SEX",
                                     var_age_cut="age_cut",
                                     color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                                     landscape = FALSE,
                                     list_graph = FALSE,
                                     canreg_header=NULL,
                                     return_data = FALSE,
                                     skin=TRUE,
                                     plot_caption= NULL,
                                     canreg_report=FALSE) {
  
  
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
  setnames(dt, var_by, "CSU_BY")
  
  dt$CSU_BY <- factor(dt$CSU_BY)
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], cases_plot:= CSU_CASES*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], cases_plot:= CSU_CASES]
  
  factor_order <- unique(dt[, c("CSU_BAR", var_age_cut), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = setkeyv(factor_order, var_age_cut)$CSU_BAR) 
  
  tick_minor_list <- csu_tick_generator(max = max(dt$CSU_CASES), 0)$tick_list
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
    scale_y_continuous(name = "Number of new cases",
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
  
  if(!canreg_report){
    print(csu_plot)
  } 
  else {
    return(csu_plot)
  }
  
}


canreg_cases_age_pie <- function(
                        df_data = dt_all,
                        var_cases = "CASES",
                        var_bar  =  "group_label",
                        color_age = c("#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3", "#a6d854"),
                        list_graph  =  FALSE,
                        plot_subtitle = "Male",
                        canreg_header  = NULL,
                        canreg_report=FALSE) {
  
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

  
  dt[,text_size:=2]
  dt[percent < 0.1,text_size:=1]

  
  
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
    
  if (!canreg_report) {
    csu_plot <- csu_plot + guides(fill=guide_legend(reverse = TRUE))
  } else {
    csu_plot <- csu_plot +   guides(fill=guide_legend(title.vjust=0, label.vjust=0, reverse = TRUE))
  }
    
  return(csu_plot)
  
}




canreg_asr_trend_top <- function(dt, var_asr="asr", 
                                 var_cases= "CASES", 
                                 var_year= "YEAR",
                                 group_by="cancer_label",
                                 log_scale = TRUE,
                                 number = 5,
                                 ytitle=NULL,
                                 landscape = FALSE,
                                 list_graph = FALSE,
                                 return_data = FALSE,
                                 canreg_header="",
                                 canreg_report=FALSE) {
  
  
  dt <- csu_dt_rank(dt,
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
  dt[[group_by]] <-csu_legend_wrapper(dt[[group_by]], 14)
  
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
    dt_label_order <- setkey(unique(dt_plot[, c(group_by, "CSU_RANK"), with=FALSE]), CSU_RANK)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = dt_label_order$cancer_label) 
    
    color_cancer <- csu_cancer_color(cancer_list =dt_label_order$cancer_label)

    
    
    plotlist[[j]] <- csu_trend_core(dt_plot,
                                    var_trend = "asr",
                                    var_year = "YEAR",
                                    group_by = "cancer_label",
                                    logscale = log_scale,
                                    smoothing = NULL,
                                    ytitle = ytitle,
                                    canreg_header = canreg_header,
                                    plot_subtitle = paste0("Top ",number, " cancer sites\n",i),
                                    plot_caption = plot_caption,
                                    color_cancer = color_cancer)$csu_plot
    
    j <- j+1
  }
  
  
  if(!canreg_report) {
    print(plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))
    print(plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)))
  } else {
    return(list(male=plotlist[[1]], female=plotlist[[2]]))
  }
  
}


csu_trend_core <- function (
  df_data,
  var_trend = "asr",
  var_year = "year",
  group_by = NULL,
  logscale = TRUE,
  smoothing = 0.3,
  legend = csu_trend_legend(),
  ytitle = "Age standardized rate per 100000",
  canreg_header = "test", 
  plot_subtitle = NULL,
  plot_caption = NULL,
  color_cancer= NULL) {
  
  linesize <- 0.5
  
  if (!is.null(smoothing)) {
    if (smoothing == 0) {
      smoothing <- NULL
    }
  }
  
  bool_dum_by <- FALSE
  if (is.null(group_by)) {
    
    df_data$CSU_dum_by <- "dummy_by"
    group_by <- "CSU_dum_by"
    bool_dum_by <- TRUE
  }
  
  dt_data <- data.table(df_data, key = group_by)
  setnames(dt_data, var_year, "CSU_Y")
  setnames(dt_data, var_trend, "CSU_T")
  setnames(dt_data, group_by, "CSU_BY")
  


  #smooth with loess  fonction
  if (!is.null(smoothing))
  {
    dt_data[,CSU_T:= loess( CSU_T ~ CSU_Y, span=smoothing)$fitted, by=CSU_BY]
  }
  
  dt_data[, max_year:=max(CSU_Y), by=CSU_BY]
  
  # to calcul y axes breaks
  tick <- csu_tick_generator(max = max(dt_data$CSU_T), min=min(dt_data[CSU_T != 0,]$CSU_T), log_scale = logscale )
  tick_space <- tick$tick_list[length(tick$tick_list)] - tick$tick_list[length(tick$tick_list)-1]
  
  
  #to calcul year axes break

  year_tick <- csu_year_tick_generator(min(dt_data$CSU_Y),max(dt_data$CSU_Y))
  

  
  
  temp_top <- ceiling(max(dt_data$CSU_T)/tick_space)*tick_space
  temp_expand_y <- max(dt_data$CSU_T)/35
  temp_expand_y_up <- max(dt_data$CSU_T)+temp_expand_y
  if (temp_expand_y_up > temp_top-(tick_space/2)) {
    temp_expand_y_up <- temp_top+temp_expand_y
  }
  
  th_legend <- list(theme(legend.position="none"))
  
  if (!bool_dum_by & legend$position == "bottom") {
    
    th_legend <- list(theme(
      legend.key = element_rect(fill="transparent"),
      legend.position = "bottom",
      legend.text = element_text(size = 12),
      legend.title = element_text(size = 12),
      legend.key.size=unit(1,"cm"),
      legend.margin = margin(0, 0, 0, 0)
    ))
  }
  

  xlim_inf <- min(c(year_tick$tick_list, year_tick$tick_minor_list))
  xlim_sup <- max(c(year_tick$tick_list, year_tick$tick_minor_list))
  

  
  #csu_plot
  if (logscale) {
    base_plot <- ggplot(dt_data[, CSU_T := ifelse(CSU_T==0,NA, CSU_T )], aes(CSU_Y, CSU_T))
  } else {
    base_plot <- ggplot(dt_data, aes(CSU_Y, CSU_T))
  }
  
  csu_plot <- base_plot+
    geom_line(aes(color=CSU_BY), size = 0.75,na.rm=TRUE)+
    guides(color = guide_legend(override.aes = list(size=0.75)))+
    labs(title = canreg_header, 
         subtitle = plot_subtitle,
         caption = plot_caption)+
    scale_x_continuous(name = "Year",
                       breaks=year_tick$tick_list,
                       limits=c(xlim_inf,xlim_sup),
                       minor_breaks = year_tick$tick_minor_list,
                       expand = c(0.015,0.015)
    )
  

  
  if (logscale){
    
    
    csu_plot <- csu_plot +
      scale_y_continuous(name = ytitle,
                         breaks=tick$tick_list,
                         minor_breaks = tick$tick_minor_list,
                         limits=c(tick$tick_list[1],tick$tick_list[length(tick$tick_list)]),
                         labels=csu_axes_label,
                         trans = "log10"
      )
  } else {
    
    csu_plot <- csu_plot +
      coord_cartesian( ylim=c(-temp_expand_y, temp_expand_y_up),  expand = TRUE)+
      scale_y_continuous(name = ytitle,
                         breaks=tick$tick_list,
                         labels=csu_axes_label,
                         expand = c(0,0)
      )
  } 
  
  if (is.null(color_cancer)) {
    csu_plot <- csu_plot +scale_colour_discrete(name=legend$title)
  } 
  else {
    csu_plot <- csu_plot +scale_colour_manual(name=NULL, values=color_cancer)
  }
  

  csu_plot <- csu_plot +
    theme(
      plot.background= element_blank(),
      panel.background = element_blank(),
      panel.grid.major= element_line(colour = "grey70"),
      panel.grid.minor= element_line(colour = "grey70"),
      plot.title = element_text(size=16, margin=margin(0,0,15,0),hjust = 0.5),
      plot.subtitle = element_text(size=15, margin=margin(0,0,15,0),hjust = 0.5),
      plot.caption = element_text(size=10, margin=margin(15,0,0,0)),
      axis.title = element_text(size=12),
      axis.title.y = element_text(margin=margin(0,15,0,0)),
      axis.title.x = element_text(margin=margin(15,0,0,0)),
      plot.margin=margin(20,20,20,20),
      axis.text = element_text(size=12, colour = "black"),
      axis.text.x = element_text(size=12,  hjust = 0.5),
      axis.ticks= element_line(colour = "black", size = linesize),
      axis.ticks.length = unit(0.2, "cm"),
      axis.line.x = element_line(colour = "black", 
                                 size = linesize, linetype = "solid"),
      axis.line.y = element_line(colour = "black", 
                                 size = linesize, linetype = "solid")
    )+
    th_legend
  
  
  
  if (!bool_dum_by & legend$position=="right") {
    
    csu_plot <- csu_plot + 
      geom_text(data = dt_data[CSU_Y == max_year, ],
                aes(label = CSU_BY),
                hjust=0,
                nudge_x=0.5)+
      theme(plot.margin = unit(c(0.5, legend$right_space_margin, 0.5, 0.5), "lines"))
    
  } else {
    
    csu_plot <- csu_plot +
      guides(color = guide_legend(nrow=legend$nrow))
  }
  
  return(list(csu_plot = csu_plot))
  
  
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


csu_dt_rank <- function(dt,
                        var_value = "CASES",
                        var_rank = "cancer_label",
                        group_by = NULL,
                        number = NULL) {
  
  bool_dum_by <- FALSE
  if (is.null(group_by)) {
    
    dt$CSU_dum_by <- "dummy_by"
    group_by <- "CSU_dum_by"
    bool_dum_by <- TRUE
  }
  
  dt <- as.data.table(dt)
  dt_rank <- dt[, list(rank_value=sum(get(var_value))), by=c(var_rank, group_by)]
  dt_rank[, CSU_RANK:= frank(-rank_value, ties.method="min"), by=group_by]

  if (!is.null(number)){
    dt_rank <- dt_rank[CSU_RANK <= number,c(group_by, var_rank, "CSU_RANK"), with=FALSE]
  }

  dt <- merge(dt_rank, dt,by=c(group_by, var_rank), all.x=TRUE)
  
  if (bool_dum_by) {
    
    dt[,CSU_dum_by:=NULL]
    
  }
  
  return(dt)
  
} 

csu_tick_generator <- function(max,min = 0,log_scale=FALSE) {
  
  
  temp_log_max = 10^floor(log10(max))
  temp_unit_floor_max = floor(max/(temp_log_max))
  
  if (!log_scale) {
    
    if (temp_unit_floor_max < 2) {
      tick_space = 0.2*temp_log_max
    } else {
      if (temp_unit_floor_max < 5) {
        tick_space = 0.5*temp_log_max
      } else {
        tick_space = temp_log_max
      }
    }
    
    temp_top <- ceiling(max/tick_space)*tick_space
    tick_list <- seq(0, temp_top, tick_space)
    tick_minor_list <- NULL
    
  } else {
    
    temp_log_min <- 10^floor(log10(min))
    temp_unit_floor_min <- floor(min/(temp_log_min))
    
    if (temp_log_min == temp_log_max) {
      
      tick_list <- c(temp_unit_floor_min:(temp_unit_floor_max+1)*temp_log_min)
      
      
      if (temp_unit_floor_max == temp_unit_floor_min) {
        tick_minor_list <- c((temp_unit_floor_min*temp_log_min)+0:9*(temp_log_min/10)) 
      } else {
        tick_minor_list <- c((temp_unit_floor_min*temp_log_min)+0:19*(temp_log_min/10)) 
      }
      
    } else if (temp_log_max/temp_log_min < 1000) {
      
      
      if (temp_unit_floor_min < 6) {
        tick_list <- temp_unit_floor_min:5*temp_log_min 
        tick_list <- c(tick_list, temp_log_min*7) ## min . . 5 7
      } else  {
        tick_list <- temp_unit_floor_min*temp_log_min ## min 
      }
      
      tick_minor_list <- temp_unit_floor_min:19*temp_log_min ## min .  . 19
      
      while (temp_log_min != (temp_log_max/10)) {
        temp_log_min = temp_log_min*10 
        tick_list <- c(tick_list, c(1,2,3,5,7)*temp_log_min)
        tick_minor_list <- c(tick_minor_list, 2:19*temp_log_min)
      }
      
      tick_minor_list <- c(tick_minor_list, 2:(temp_unit_floor_max+1)*temp_log_max)
      
      if (temp_unit_floor_max <5) {
        tick_list <- c(tick_list, 1:(temp_unit_floor_max+1)*temp_log_max)
      } else if (temp_unit_floor_max <7) {
        tick_list <- c(tick_list, c(1,2,3,5,temp_unit_floor_max+1)*temp_log_max)
      } else {
        tick_list <- c(tick_list, c(1,2,3,5,7,temp_unit_floor_max+1)*temp_log_max)
      }
      
    } else {
      
      if (temp_unit_floor_min == 1) {
        tick_list <- c(1,2,3,5)*temp_log_min
      } else  if (temp_unit_floor_min == 2) {
        tick_list <- c(2,3,5)*temp_log_min
      } else  if (temp_unit_floor_min < 6) {
        tick_list <- c(5,7)*temp_log_min
      } else {
        tick_list <- 7*temp_log_min
      }
      tick_minor_list <- temp_unit_floor_min:9*temp_log_min ## min .  . 19
      
      while (temp_log_min != (temp_log_max/10)) {
        temp_log_min = temp_log_min*10 
        tick_list <- c(tick_list, c(1,2,5)*temp_log_min)
        tick_minor_list <- c(tick_minor_list, 2:9*temp_log_min)
      }
      
      tick_minor_list <- c(tick_minor_list, 2:(temp_unit_floor_max+1)*temp_log_max)
      
      if (temp_unit_floor_max <5) {
        tick_list <- c(tick_list, unique(c(1,2,temp_unit_floor_max+1)*temp_log_max))
      } else if (temp_unit_floor_max <6) {
        tick_list <-c(tick_list, c(1,2,5)*temp_log_max)
      } else if (temp_unit_floor_max < 7) {
        tick_list <- c(tick_list, c(1,2,5,7)*temp_log_max)
      } else {
        tick_list <- c(tick_list, c(1,2,5,7,temp_unit_floor_max+1)*temp_log_max)
      }
      
      
      
    }
    
  }
  
  return(list(tick_list=tick_list, tick_minor_list=tick_minor_list))
  
}

csu_year_tick_generator <- function(min, max) {
  
  mod <- 5
  if (max - min < 10 ) {
    mod <- 1 
  } else if (max - min < 20){
    mod <- 2
  } 
  

  temp1 <- min - (min %% mod)
  temp2 <- max - (max %% mod) +ifelse(mod>=5,mod,0)
  
  if (temp2 - temp1 <= mod*6) {
    year_space <- mod 
    year_list <- seq(temp1,temp2,year_space)
    year_minor_list <- year_list
    
  } else  {
    year_space <- mod*2 
    if (temp1 %% mod*2 > 0) {
      year_list <- seq(temp1+mod,temp2,year_space)
      year_minor_list <-  seq(temp1,temp2,year_space/2)
    } else {
      year_list <- seq(temp1,temp2,year_space)
      year_minor_list <-  seq(temp1,temp2,year_space/2)
    }
  }
  
  return(list(tick_list=year_list, tick_minor_list=year_minor_list))
  
}


csu_axes_label <- function(l) {
  
  l <- format(l, big.mark = ",", scientific = FALSE, drop0trailing = TRUE)
  
}

csu_legend_wrapper <- function(label, width) {
  
  label <- sapply(strwrap(label, width = width, simplify = FALSE), paste, collapse="\n")
  return(label)
  
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
  
  cancer_color <- c("#AE563E", "#AE563E",
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
  
  
  dt_color <- data.table(cancer_label = cancer_base, cancer_color= cancer_color)
  cancer_list <-  gsub("\n"," ", cancer_list)
  dt_cancer_list <- data.table(cancer_label = cancer_list)
  dt_color_map <- merge(dt_cancer_list, dt_color, by = c("cancer_label"), all.x=TRUE, sort=F )
  color_map <- c(dt_color_map$cancer_color)

  return(color_map)
  
}





extract_legend_axes<-function(a_gplot){
  pdf(file=NULL)
  tmp <- ggplotGrob(a_gplot)
  leg_index <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box")
  xlab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.x"))
  ylab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.y"))
  title_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.title"))
  caption_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.capti"))
  legend <- tmp$grobs[[leg_index]]
  xlab <- tmp$grobs[[xlab_index]]
  ylab <- tmp$grobs[[ylab_index]]
  title <- tmp$grobs[[title_index]]
  caption <- tmp$grobs[[caption_index]]
  dev.off()
  return(list(legend=legend, xlab=xlab, ylab=ylab, title=title, caption=caption))
}