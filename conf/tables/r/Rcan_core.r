
canreg_load_packages <- function(packages_list) { 
  
  
  missing_packages <- packages_list[!(packages_list %in% installed.packages()[,"Package"])]
  
  old.repos <- getOption("repos") 
  on.exit(options(repos = old.repos)) #this resets the repos option when the function exits 
  new.repos <- old.repos 
  new.repos["CRAN"] <- "http://cran.stat.ucla.edu" #set your favorite  CRAN Mirror here 
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
  
  missing_packages <- unique(missing_packages)
  
  if(length(missing_packages) > 0 ) {
    for (i in missing_packages) {
      install.packages(i, dependencies=  c("Depends", "Imports", "LinkingTo"))
    }
  }
  
  lapply(packages_list, require, character.only = TRUE)
  
}


canreg_getArgs <- function(Args, variable, boolean=FALSE) {
  
  length_variable <-  nchar(variable)
  variable_name_list <- substr(Args, 1, length_variable)
  
  if (variable %in% variable_name_list) {
    
    if (boolean) {
      return(TRUE)
    } else {
      var_nb <- which(variable_name_list == variable)
      return(substring(Args[var_nb], (length_variable+2), nchar(Args[var_nb]) ))
    }
    
  } else {
    if (boolean) {
      return(FALSE)
    } else {
      stop(variable, " is not in the list of option return by canreg")
    }
  }
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


canreg_ageSpecific_rate_data <- function(dt, keep_ref=FALSE) { 
  
  var_by <- c("ICD10GROUP", "ICD10GROUPLABEL", "AGE_GROUP","AGE_GROUP_LABEL", "SEX")
  if (keep_ref) {
    var_by <- c(var_by, "REFERENCE_COUNT")
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
        cat("\n")
        cat("Population with less than 18 age group:\n\n" )
        print
        print(unique(temp), row.names = FALSE)
        cat("\n")
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
    cat("ASR have been computed for the age group ", (first_age-1)*5,"-", temp , "\n",  sep="" )
    temp<- NULL
    
  } else {
    
    cat("ASR have been computed for the age groups:\n",age_group_list , "\n",  sep="" )
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
        legend.text = element_text(size = 12),
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
        geom_point(aes(fill=CSU_BY), size = 3,na.rm=TRUE,shape=21,stroke=0.5,colour="black", show.legend=FALSE)+
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
        axis.title = element_text(size=12),
        axis.title.y = element_text(margin=margin(0,15,0,0)),
        axis.title.x = element_text(margin=margin(15,0,0,0)),
        axis.text = element_text(size=12, colour = "black"),
        axis.text.x = element_text(size=12, angle = 60,  hjust = 1),
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
                                               var_age_label_list = "AGE_LABEL",
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
    plot.caption = element_blank()
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
                                 age_label_list = dt[[var_age_label_list]]
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
      heights=heights
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

canreg_ageSpecific_rate_top <- function(dt, var_age="AGE_GROUP", 
                                        var_cases= "CASES", 
                                        var_py= "COUNT",
                                        var_by="SEX",
                                        var_age_label_list = "AGE_LABEL",
                                        log_scale = TRUE,
                                        nb_top = 5,
                                        landscape = FALSE,
                                        list_graph = FALSE,
                                        return_data = FALSE,
                                        canreg_header="") {
  
  
  dt_rank <- dt[, list(total= sum(CASES)), by=c(var_by, "cancer_label")]
  dt_rank[, cancer_rank:= frank(-total, ties.method="min"), by=var_by]
  dt_rank <- dt_rank[cancer_rank <= nb_top,c(var_by, "cancer_label", "cancer_rank"), with=FALSE] 
  dt <- merge(dt_rank, dt,by= c("SEX", "cancer_label"), all.x=TRUE)
  dt$cancer_label <-csu_legend_wrapper(dt$cancer_label, 17)
  
  
  if (return_data) {
    dt[, rate := CASES/COUNT*10000]
    dt[, cancer_sex := NULL]
    dt[, cancer_title := NULL]
    dt[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
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
    dt_label_order <- setkey(unique(dt_plot[, c("cancer_label", "cancer_rank"), with=FALSE]), cancer_rank)
    dt_plot$cancer_label <- factor(dt_plot$cancer_label,levels = dt_label_order$cancer_label) 
    plotlist[[j]] <- csu_ageSpecific_core(dt_plot,
                                          var_age=var_age,
                                          var_cases= var_cases,
                                          var_py=var_py,
                                          var_by = "cancer_label",
                                          plot_title = plot_title,
                                          plot_subtitle = paste0("Top ",nb_top, " cancer sites\n",i),
                                          plot_caption = plot_caption,
                                          color_trend = NULL,
                                          log_scale = log_scale,
                                          age_label_list = dt_plot[[var_age_label_list]],
    )$csu_plot
    
    j <- j+1
  }
  
  print(plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=0.75), nrow=1,byrow=TRUE)))
  print(plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=0.75), nrow=1,byrow=TRUE)))
  
}



canreg_ASR_bar_top <- function(df_data,
                               var_asr = "asr",
                               var_bar = "cancer_label",
                               var_by = "SEX",
                               nb_top = 10,
                               db_rate = 100000,
                               color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                               landscape = FALSE,
                               list_graph = FALSE,
                               canreg_header=NULL,
                               canreg_age_group = "",
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
  
  line_size <- 0.1
  text_size <- 14
  
  setnames(dt, var_asr, "CSU_ASR")
  setnames(dt, var_bar, "CSU_BAR")
  setnames(dt, var_by, "CSU_BY")
  
  plot_subtitle <- paste("top",nb_top,"cancer sites")
  
  dt_rank <- dt[, list(asr_both=sum(CSU_ASR)), by=CSU_BAR]
  dt_rank <- dt_rank[asr_both > 0,]
  dt_rank[, cancer_rank:= frank(-asr_both, ties.method="min"),]
  dt_rank <- dt_rank[cancer_rank <= nb_top,]
  dt <- merge(dt_rank, dt,by=c("CSU_BAR"), all.x=TRUE)
  
  
  if (return_data) {
    dt[, asr_both := NULL]
    setnames(dt, "CSU_BAR",var_bar)
    setnames(dt, "CSU_BY", var_by)
    setnames(dt, "CSU_ASR", var_asr)
    setkeyv(dt, c("cancer_rank",var_bar))
    return(dt)
    stop() 
  }
  
  
  
  dt$CSU_BAR <-csu_legend_wrapper(dt$CSU_BAR, 15)
  dt[CSU_BY==levels(dt$CSU_BY)[[1]], asr_plot:= CSU_ASR*(-1)]
  dt[CSU_BY==levels(dt$CSU_BY)[[2]], asr_plot:= CSU_ASR]
  
  dt$CSU_BAR <- factor(dt$CSU_BAR)
  factor_order <- unique(dt[, c("CSU_BAR", "cancer_rank"), with=FALSE])
  dt$CSU_BAR <- factor(dt$CSU_BAR,
                       levels = rev(setkeyv(factor_order, "cancer_rank")$CSU_BAR)) 
  
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
  dt$asr_round <-  format(round(dt$CSU_ASR, digits = 1), nsmall = 1)
  
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
    scale_y_continuous(name = paste0("Age-standardized incidence rate per ", formatC(db_rate, format="d", big.mark=","), ", ", canreg_age_group),
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




canreg_output <- function(output_type="pdf",filename=NULL, landscape = FALSE,list_graph = TRUE, FUN,...) {
  
  
  paper <- ifelse(landscape, "a4r", "a4")
  png_width <- ifelse(landscape, 2339 , 1654 )
  png_height <- ifelse(landscape, 1654 , 2339 )
  tiff_width <- ifelse(landscape, 3508 , 2480 )
  tiff_height <- ifelse(landscape, 2480 , 3508 )
  svg_width <- ifelse(landscape, 11.692 , 8.267 )
  svg_height <- ifelse(landscape, 8.267 , 11.692 )
  pdf_width <- ifelse(landscape, 11.692 , 8.267 )    # Needs tuning
  pdf_height <- ifelse(landscape, 8.267 , 11.692 )   # Needs tuning
  
  file_number <- ifelse(list_graph, "%03d", "")
  
  
  ## create output plot
  
  
  if (is.null(output_type)) {
    FUN(..., landscape=landscape, list_graph=list_graph)
  } else if (output_type == "ps") {
    postscript(paste(filename,".ps", sep=""),width = svg_width, height = svg_height)
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "png") {
    png(paste(filename,file_number,".png", sep=""),width = png_width, height = png_height, units = "px",res = 200) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "tiff") {
    tiff(paste(filename,file_number,".tiff", sep=""),width = tiff_width, height = tiff_height, units = "px",res = 300,compression ="jpeg" ) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  }else if (output_type == "svg") {
    svg(paste(filename,file_number,".svg", sep=""),width = svg_width, height = svg_height,) 
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  }else if (output_type == "pdf") {
    # pdf(paste(filename,".pdf", sep=""), paper = paper, width = 0, height = 0)
    CairoPDF(paste(filename,".pdf", sep=""), width = pdf_width, height = pdf_height)
    FUN(..., landscape=landscape, list_graph=list_graph)
    dev.off()
  } else if (output_type == "csv") {
    df_data <- FUN(..., return_data=TRUE)
    write.csv(df_data, paste(filename,".csv", sep=""),
              row.names = FALSE)
  }
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



csu_axes_label <- function(l) {
  
  l <- format(l, big.mark = ",", scientific = FALSE, drop0trailing = TRUE)
  
}

csu_legend_wrapper <- function(label, width) {
  
  label <- sapply(strwrap(label, width = width, simplify = FALSE), paste, collapse="\n")
  return(label)
  
}




extract_legend_axes<-function(a_gplot){
  pdf(file=NULL)
  tmp <- ggplotGrob(a_gplot)
  leg_index <- which(sapply(tmp$grobs, function(x) x$name) == "guide-box")
  xlab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.x"))
  ylab_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,12 ) == "axis.title.y"))
  title_index <- which(sapply(tmp$grobs, function(x) substr(x$name, 1,10 ) == "plot.title"))
  legend <- tmp$grobs[[leg_index]]
  xlab <- tmp$grobs[[xlab_index]]
  ylab <- tmp$grobs[[ylab_index]]
  title <- tmp$grobs[[title_index]]
  dev.off()
  return(list(legend=legend, xlab=xlab, ylab=ylab, title=title))
}