

shiny_data <- function(input) {
  
  dt_temp <- NULL
  if  (!is.null(input$select_table)) {
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
			temp <- (canreg_age_group$last_age)*5
			
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
			
			
			if (!is.null(input$slideAgeRange)& !is.null(input$radioValue)) {
		
				dt_temp <- dt_base
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
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
			
			
			if (!is.null(input$slideAgeRange) & !is.null(input$radioValue)) {
		
				dt_temp <- dt_base
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
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
		else if (table_number == 9){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp <- dt_base
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL")  
			}
				
		}
		else if (table_number == 10){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp <- dt_base
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL")

																	
			}
				
		}
		else if (table_number == 11){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp <- dt_base
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				
				first_age <- (input$slideAgeRange[1]/5)+1
				last_age <- input$slideAgeRange[2]/5
				max_age <- canreg_age_group$last_age+1 
				if (last_age >= max_age) last_age <- 18
				
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE, keep_year = TRUE)
				
				dt_temp<- Rcan:::core.csu_asr(df_data =dt_temp, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
																				group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_temp),
																				first_age = first_age,
																				last_age= last_age,
																				pop_base_count = "REFERENCE_COUNT",
																				age_label_list = "AGE_GROUP_LABEL")  
																				
				dt_temp <- as.data.table(dt_temp)
			}
				
		}
	}
 
  return(dt_temp)
  
}




shiny_plot <- function(dt_plot,input, download = FALSE,slide=FALSE, file = NULL) {
  
	if  (!is.null(input$select_table)) {
  
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
		
			if (!is.null( input$slideNbTopBar)& !is.null(input$radioValue)) {
			
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 
				

				
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
		
			if (!is.null( input$slideNbTopBar)& !is.null(input$radioValue)) {
			
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1

				
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
					
					
					canreg_ageSpecific(
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_subtitle = isolate(input$selectCancerSite),
								color_trend = color_trend
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
		else if (table_number == 9){
			
		 if (!is.null( input$slideNbTopBar) & !is.null(input$radioLog)) {
				
				if (input$radioLog == "log") {
					bool_log <- TRUE
				}
				else {
					bool_log <- FALSE
				}
				
				nb_top <- input$slideNbTopBar
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				 ytitle <- paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
		 

				if (download) {
				
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = TRUE,
								FUN=canreg_asr_trend_top,
								dt=dt_plot,number = nb_top,
								canreg_header = ls_args$header,
								logscale = bool_log,
								ytitle=ytitle)

				}
				else {
					temp <- canreg_asr_trend_top(
						dt=dt_plot,number = nb_top,
						canreg_header = ls_args$header,
						logscale = bool_log,
						return_plot=TRUE,
						ytitle=ytitle)
						
						
				grid.arrange(
					temp$plotlist[[1]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)),
					temp$plotlist[[2]]+guides(color = guide_legend(override.aes = list(size=1), nrow=1,byrow=TRUE)),
					ncol=2)

				}
			}
				
			
		}
		else if (table_number == 10){
			
		 if (!is.null( input$slideNbTopBar) & !is.null(input$checkCI)) {
				
				bool_CI <- input$checkCI
				nb_top <- input$slideNbTopBar
				
				if (bool_CI) {
					group_by <- "SEX"
					graph_list <- TRUE
				} else{
					group_by <- NULL
					graph_list <- FALSE
				}
				
				dt_plot <- Rcan:::core.csu_dt_rank(dt_plot,
									var_value= "CASES",
									var_rank = "cancer_label",
									group_by = group_by,
									number = nb_top)
									
				dt_plot <- Rcan:::core.csu_eapc(dt_plot, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX",  "CSU_RANK"))
				dt_plot <-as.data.table(dt_plot)		
				
				
				
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				 ytitle <- paste0("Estimated average percentage change (%), ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
				 color_bar <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
		 

				if (download) {
				
					if (bool_CI) {
					
							canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = graph_list,
									FUN=canreg_eapc_scatter_error_bar,
									dt=dt_plot,
									canreg_header = ls_args$header,
									ytitle=ytitle)
					
					
					
					}
					else {
						canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = graph_list,
									FUN=canreg_eapc_scatter,
									dt_plot=dt_plot,color_bar=color_bar,
									canreg_header = ls_args$header,
									ytitle=ytitle)
					}

				}
				else {
					if (bool_CI) {
					
						temp <- canreg_eapc_scatter_error_bar(
								dt=dt_plot,
								canreg_header = ls_args$header,
								return_plot = TRUE, 
								ytitle=ytitle)
								
						grid.arrange(temp$plotlist[[1]], temp$plotlist[[2]], ncol=2)
					
					}
					else {
					
						canreg_eapc_scatter(
							dt_plot=dt_plot,color_bar=color_bar,
							canreg_header = ls_args$header,
							ytitle=ytitle)
					}
				}
			}
				
			
		}
		else if (table_number == 11){
			
			if (!is.null( input$selectCancerSite) & !is.null(input$radioLog)) {
			
				bool_log <- (input$radioLog == "log")
				color_trend <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				ytitle <- paste0("Age-standardized incidence rate per ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " years old" )
		 
				
				if (download) {
				 
					canreg_output(output_type = output_type, filename =file,landscape = FALSE,list_graph = FALSE,
								FUN=canreg_asr_trend,
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_title=ls_args$header,
								ytitle = ytitle
								)
							
				}
				else {
					
					
					canreg_asr_trend(
								dt_plot=dt_plot,
								logscale = bool_log,
								plot_title=ls_args$header,
								ytitle = ytitle
								)
								
				}
				
			
			
			}
		
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
 
 
 canreg_asr_trend <- function(dt_plot,
																 var_asr="asr", 
                                 var_cases= "CASES", 
                                 var_year= "YEAR",
                                 group_by="SEX",
                                 logscale = TRUE,
                                 ytitle=NULL,
                                 landscape = FALSE,
                                 list_graph = FALSE,
                                 return_data = FALSE,
																 return_plot= FALSE,
                                 plot_title="") {
  
 
  
  if (return_data) {
    dt <- dt[, c(group_by,var_year,var_asr), with=FALSE]
    setkeyv(dt, c("SEX",var_year))
    return(dt)
    stop() 
  }
  
	
	color_trend <- c("Male" = "#2c7bb6", "Female" = "#b62ca1")
    
   plot <- Rcan:::core.csu_time_trend(dt_plot,
                                    var_trend = "asr",
                                    var_year = "YEAR",
                                    group_by = "SEX",
                                    logscale = logscale,
                                    smoothing = NULL,
                                    ytitle = ytitle,
                                    plot_title = plot_title,
                                    color_trend = color_trend)$csu_plot

																		
  
  print(plot)

}

										
#function for multiple file download output_type
multiple_output <- function(table_number, bool_ci, output_format) {
	
	bool_temp <- FALSE
	if (output_format %in% c("png", "tiff", "svg")) {
		if (table_number %in% c(5,6,9)) {
			bool_temp <- TRUE
		}
		else if (table_number == 10 & bool_ci) {
			bool_temp <- TRUE
		}
	}
	
	return(bool_temp)

}                                        
										

shiny_error_log <- function(log_file,filename) {
  
  error_connection <- file(log_file,open="wt")
  sink(error_connection)
  sink(error_connection, type="message")
  
  #print error
  cat("This file contains the data and parameter of this canreg5 R-shiny application.\n") 
  cat(paste0("If an error occured, please restart this application send this log file: `",filename,"` to canreg@iarc.fr, with a description of the error\n"))
  cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
  cat("\n")
  
  #print argument from canreg
  print(ls_args)
  cat("\n")
  
  #print environment
  print(ls.str())
  cat("\n")
	
	#print R version and package load
  print(sessionInfo())
  cat("\n")

  #print missing package
  packages_list <- c("Rcpp", "data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png")

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
  dput(read.table(ls_args$inc, header=TRUE, sep="\t"))
  cat("\n")
  cat("population file\n")
  dput(read.table(ls_args$pop, header=TRUE, sep="\t"))
  cat("\n")
	
	#close log_file and send to canreg
  sink(type="message")
  sink()
  close(error_connection)
  
}

shiny_dwn_data <- function(log_file) {

	
	dt_base[, ICD10GROUPLABEL := NULL]
	dt_base[, ICD10GROUPCOLOR := NULL]
	dt_base[, AGE_GROUP := NULL]
	
	write.csv(dt_base, paste0(log_file),row.names = FALSE)

}
