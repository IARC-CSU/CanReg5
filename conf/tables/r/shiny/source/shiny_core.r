

shiny_data <- function(input) {
  
  dt_temp <- NULL
  
  table_number <- input$select_table
  if ( table_number == 1) {
    dt_temp <- canreg_pop_data(pop_file =ls_args$pop)
    
    
  }
  else if (table_number == 2){
    
    
    if (!is.null(input$radioSkin) & !is.null(input$radioAgeGroup)) {
      

      if (input$radioSkin == 1 ){
        bool_skin <- FALSE
      }
      else {
        bool_skin <- TRUE
      }
	  
	  if (input$radioAgeGroup == 1 ){
		temp <- (canreg_age_group$last_age-2)*5
		
        age_group = seq(5,temp,5)
      }
      else {
        age_group  <- c(15,30,50,70)
      }
		  
	  dt_temp <- dt_base
	  dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
	  dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
	  dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
	  
	  
	  
	  
	  #dt_report <- canreg_age_cases_data(dt_report,age_group = c(5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80), skin=TRUE)
	  
      
      dt_temp <- canreg_age_cases_data(dt_temp,age_group= age_group, skin=bool_skin)
      
    }
    
  }
  else if (table_number == 3){
    
    
    if (!is.null(input$radioSkin)) {
      

      if (input$radioSkin == 1 ){
        bool_skin <- FALSE
      }
      else {
        bool_skin <- TRUE
      }
	  
	 
		  
	  dt_temp <- dt_base
	  dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
	  dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
	  dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
      
      dt_temp <- canreg_age_cases_data(dt_temp, skin=bool_skin)
      
    }
    
  }
  else if (table_number == 4){
    
    
    if (!is.null(input$slideAgeRange)) {
	
			dt_temp <- dt_base
			dt_temp <- dt_temp[ICD10GROUP != "C44",]
			dt_temp <- dt_temp[ICD10GROUP != "O&U",]
      
			first_age <- (input$slideAgeRange[1]/5)+1
      last_age <- input$slideAgeRange[2]/5
      max_age <- canreg_age_group$last_age 
      if (last_age >= max_age) last_age <- 18
      
      dt_temp <- canreg_ageSpecific_rate_data( dt_temp, keep_ref = TRUE)
      
      if (isolate(input$radioValue) == "cum") {
        if (last_age > 15) last_age <-15
        dt_temp <- csu_cum_risk_core(df_data =dt_temp,
                                     var_age="AGE_GROUP", var_cases="CASES", var_py="COUNT",
                                     group_by = c("cancer_label", "SEX"),
                                     missing_age = canreg_missing_age(dt_temp),
                                     age_label_list = "AGE_GROUP_LABEL",
                                     last_age= last_age)
      }
      else{
      
      
      dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX"), missing_age = canreg_missing_age(dt_temp),
                                    first_age = first_age,
                                    last_age= last_age,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
      
      }
    
    }
    
  }
	else if (table_number == 5){
    
    
    if (!is.null(input$slideAgeRange)) {
	
			dt_temp <- dt_base
			dt_temp <- dt_temp[ICD10GROUP != "C44",]
			dt_temp <- dt_temp[ICD10GROUP != "O&U",]
      
			first_age <- (input$slideAgeRange[1]/5)+1
      last_age <- input$slideAgeRange[2]/5
      max_age <- canreg_age_group$last_age 
      if (last_age >= max_age) last_age <- 18
    
      dt_temp <- canreg_ageSpecific_rate_data( dt_temp, keep_ref = TRUE)
      
      if (isolate(input$radioValue) == "cum") {
        if (last_age > 15) last_age <-15
        dt_temp <- csu_cum_risk_core(df_data =dt_temp,
                                     var_age="AGE_GROUP", var_cases="CASES", var_py="COUNT",
                                     group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"),
                                     missing_age = canreg_missing_age(dt_temp),
                                     age_label_list = "AGE_GROUP_LABEL",
                                     last_age= last_age)
      }
      else{
      
      
      dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                    group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
                                    first_age = first_age,
                                    last_age= last_age,
                                    pop_base_count = "REFERENCE_COUNT",
                                    age_label_list = "AGE_GROUP_LABEL")
      
      }
    
    }
    
  }
	else if (table_number == 6){

		dt_temp <- dt_base
		dt_temp <- dt_temp[ICD10GROUP != "C44",]
		dt_temp <- dt_temp[ICD10GROUP != "O&U",]
		dt_temp <- canreg_ageSpecific_rate_data(dt_temp)
      
  }
	else if (table_number == 7){

		dt_temp <- dt_base
		dt_temp <- dt_temp[ICD10GROUP != "C44",]
		dt_temp <- dt_temp[ICD10GROUP != "O&U",]
		dt_temp <- canreg_ageSpecific_rate_data(dt_temp)
      
  }
	else if (table_number == 8){

		if (!is.null(input$radioSkin)) {
      

      if (input$radioSkin == 1 ){
        bool_skin <- FALSE
      }
      else {
        bool_skin <- TRUE
      }

			dt_temp <- dt_base
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
			dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
				
			dt_temp <- canreg_year_cases_data(dt_temp, skin=bool_skin)
      
    }
      
  }
 
  return(dt_temp)
  
}




shiny_plot <- function(dt_plot,input, download = FALSE,slide=FALSE, file = NULL) {
  

  
  if (download) {
    table_number <- input$select_table
    if (slide) {
			ls_args$header  <- ""
      output_type <- "png"
    }
    else {
      output_type <- input$select_format
    }
  }
  else {
	  ls_args$header  <- ""
    table_number <- isolate(input$select_table)
  }
  
  
  
  
  
  if ( table_number == 1) {
    
    if (download) {
      

      canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
                    FUN=canreg_population_pyramid,
                    df_data=dt_plot,
                    canreg_header = ls_args$header)
    }
    else {
      canreg_population_pyramid( df_data=dt_plot, canreg_header = ls_args$header)
    }
  
    
  }
  else if (table_number == 2){
    
    if (isolate(input$radioSkin) == 1 ){
      bool_skin  <- FALSE
    }
    else {
      bool_skin  <- TRUE
    }
    
    if (download) {
      canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
                    FUN=canreg_cases_age_bar,
                    df_data=dt_plot,
                    color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                    canreg_header = ls_args$header,
                    skin=bool_skin)
    }
    else {
      canreg_cases_age_bar(
        df_data=dt_plot,
        color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
        canreg_header = ls_args$header,
        skin=bool_skin
      )
    }
    
  }
   else if (table_number == 3){
    
    if (isolate(input$radioSkin) == 1 ){
	  header = paste0(ls_args$header, "\n\nAll cancers but C44")
    }
    else {
	  header = paste0(ls_args$header, "\n\nAll cancers")
    }
    
    if (download) {
	  canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_plot,
                canreg_header = header)
    }
    else {
      canreg_age_cases_pie_multi_plot(
        dt=dt_plot,
        canreg_header = header)
      
    }
    
  }
  else if (table_number == 4){
	
		if (!is.null( input$slideNbTopBar)) {
		
			nb_top <- input$slideNbTopBar
      last_age <- (isolate(input$slideAgeRange)[2]/5)
      max_age <- canreg_age_group$last_age # to change 
      

      
      if (last_age < max_age) {
        age2 <- isolate(input$slideAgeRange)[2]-1
      } else {
        age2 <- paste0(((max_age-1)*5), "+")
      }
      
      
      if (isolate(input$radioValue) == "asr") {
        var_top <- "asr"
        digit <- 1
        ytitle <- paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
        
        
      } 
      else if (isolate(input$radioValue) == "cases"){
        var_top <- "CASES"
        digit <- 0
        ytitle <-  paste0("Number of cases, ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
        
        
      }
      else if (isolate(input$radioValue) == "cum") {
        var_top <- "cum_risk"
        digit <- 2
        if (last_age >= 15) {
          age2 <- 74
        } else {
          age2 <- isolate(input$slideAgeRange)[2]-1
        }
        ytitle<-paste0("Cumulative incidence risk (percent), 0-",age2, " years old" )
        
        
      }
      
      if (download) {
        canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
                      FUN=canreg_bar_top,
                      df_data=dt_plot,
                      var_top = var_top,
                      color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                      nb_top = nb_top,nsmall=digit,
                      canreg_header  = ls_args$header,
                      ytitle=ytitle)
      }
      else {
        canreg_bar_top(df_data=dt_plot,
                       var_top = var_top,
                       color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                       nb_top = nb_top,nsmall=digit,
                       canreg_header = ls_args$header,
                       ytitle=ytitle)
      }
     
      
      
      
      
    }
    
  }
  
	 else if (table_number == 5){
	
		if (!is.null( input$slideNbTopBar)) {
		
			nb_top <- input$slideNbTopBar
      last_age <- (isolate(input$slideAgeRange)[2]/5)
      max_age <- canreg_age_group$last_age # to change 
      

      
      if (last_age < max_age) {
        age2 <- isolate(input$slideAgeRange)[2]-1
      } else {
        age2 <- paste0(((max_age-1)*5), "+")
      }
      
      
      if (isolate(input$radioValue) == "asr") {
        var_top <- "asr"
        digit <- 1
        ytitle <- paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
        
        
      } 
      else if (isolate(input$radioValue) == "cases"){
        var_top <- "CASES"
        digit <- 0
        ytitle <-  paste0("Number of cases, ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
        
        
      }
      else if (isolate(input$radioValue) == "cum") {
        var_top <- "cum_risk"
        digit <- 2
        if (last_age >= 15) {
          age2 <- 74
        } else {
          age2 <- isolate(input$slideAgeRange)[2]-1
        }
        ytitle<-paste0("Cumulative incidence risk (percent), 0-",age2, " years old" )
        
        
      }
      
      if (download) {
									
        canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
                      FUN=canreg_bar_top_single,
                      dt=dt_plot,
                      var_top = var_top,
                      nb_top = nb_top,digit=digit,
                      canreg_header  = ls_args$header,
                      xtitle=ytitle)
      }
      else {
				 temp <- canreg_bar_top_single(
					dt=dt_plot,
					var_top = var_top,
					nb_top = nb_top,digit=digit,
					canreg_header  = ls_args$header,
					xtitle=ytitle,
					return_plot=TRUE)
					
					

    
        grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)
					
				
					
					
					
      }
     
      
      
      
      
    }
    
  }
	else if (table_number == 6){
	
		if (!is.null( input$slideNbTopBar) & !is.null(input$radioLog)) {
		
			nb_top <- input$slideNbTopBar
			
			if (input$radioLog == "log") {
				bool_log <- TRUE
			}
			else {
				bool_log <- FALSE
			}

      if (download) {
				
				canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
							FUN=canreg_ageSpecific_rate_top,
							df_data=dt_plot,logscale = bool_log,nb_top = nb_top,
							plot_title = ls_args$header
							)
						
      }
      else {
				temp <- Rcan:::core.csu_ageSpecific_top(
					df_data=dt_plot,
					var_age="AGE_GROUP",
					var_cases= "CASES", 
					var_py= "COUNT",
					var_top = "cancer_label",
					var_age_label_list = "AGE_GROUP_LABEL",
					group_by="SEX",
					missing_age=canreg_missing_age(dt_plot),
					var_color="ICD10GROUPCOLOR",
					logscale = bool_log,
					nb_top = nb_top,
					plot_title = ls_args$header
				)
            
            
				temp$plotlist[[1]] <- temp$plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=2,byrow=TRUE))
				temp$plotlist[[2]] <- temp$plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=2,byrow=TRUE))
				grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)

      }
     
      
      
      
      
    }
    
  }
	else if (table_number == 7){
		
		if (!is.null( input$selectCancerSite) & !is.null(input$radioLog)) {
		
			bool_log <- (input$radioLog == "log")
			color_trend <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
			dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
			
			 if (download) {
			 
				canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = FALSE,
							FUN=canreg_ageSpecific,
							dt_plot=dt_plot,
							logscale = bool_log,
							plot_subtitle = isolate(input$selectCancerSite),
							color_trend = color_trend
							)
						
      }
      else {
				
				Rcan:::core.csu_ageSpecific(dt_plot,
					var_age="AGE_GROUP",
					var_cases= "CASES", 
					var_py= "COUNT",
					group_by="SEX",
					plot_title = ls_args$header,
					plot_subtitle = isolate(input$selectCancerSite),
					color_trend = color_trend,
					logscale = bool_log,
					age_label_list = unique(dt_plot[["AGE_GROUP_LABEL"]])
					
					)
			}
			
		
		
		}
	
	}
	else if (table_number == 8){
    
    if (isolate(input$radioSkin) == 1 ){
      bool_skin  <- FALSE
    }
    else {
      bool_skin  <- TRUE
    }
    
    if (download) {
      canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
                    FUN=canreg_cases_year_bar,
                    dt=dt_plot,
                    canreg_header = ls_args$header,
                    skin=bool_skin)
    }
    else {
       canreg_cases_year_bar(
				dt=dt_plot,
				canreg_header = ls_args$header,
				skin=bool_skin
			)
    }
    
  }

  
  
}



#create a new function to print the graph and return data (adapt to canreg_output function)
canreg_ageSpecific <- function(dt_plot,color_trend,plot_subtitle="",logscale=FALSE, landscape = TRUE,list_graph = FALSE, return_data = FALSE) {
	
	if (return_data) {
		
		dt_temp <- dt_plot[COUNT > 0,]
		dt_temp[, rate := CASES/COUNT*10000]
		dt_temp[, AGE_GROUP_LABEL := paste0("'",AGE_GROUP_LABEL,"'")]
		dt_temp <- dt_temp[, c("cancer_label",
								"AGE_GROUP",
								"AGE_GROUP_LABEL",
								"SEX",
								"CASES",
								"COUNT",
								"rate"), with=FALSE]
		setkeyv(dt_temp, c("SEX","AGE_GROUP"))
		return(dt_temp)
		stop() 
		
	} 
		
	plot<- Rcan:::core.csu_ageSpecific(dt_plot,
																		 var_age="AGE_GROUP",
																		 var_cases= "CASES",
																		 var_py="COUNT",
																		 group_by = "SEX",
																		 plot_title = ls_args$header,
																		 plot_subtitle = plot_subtitle,
																		 color_trend = color_trend,
																		 logscale = logscale,
																		 age_label_list = unique(dt_plot[["AGE_GROUP_LABEL"]]))$csu_plot
																		
	print(plot)

}
 
										
                                        
										
