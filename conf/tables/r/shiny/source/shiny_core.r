

shiny_data <- function(input, session) {
  
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
				
			dt_temp  <- copy(dt_base)
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
			
		 
				
			dt_temp  <- copy(dt_base)
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
			dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
			dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
				
				dt_temp <- canreg_age_cases_data(dt_temp, skin=bool_skin)
				
			}
			
		}
		else if (table_number == 4){
			
			
			if (!is.null(input$slideAgeRange)& !is.null(input$radioValue)) {
		
				dt_temp  <- copy(dt_base)
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
		
				dt_temp  <- copy(dt_base)
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

			dt_temp  <- copy(dt_base)
			dt_temp <- dt_temp[ICD10GROUP != "C44",]
			dt_temp <- dt_temp[ICD10GROUP != "O&U",]
			dt_temp <- canreg_ageSpecific_rate_data(dt_temp)
				
		}
		else if (table_number == 7){

			dt_temp  <- copy(dt_base)
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

				dt_temp  <- copy(dt_base)
				dt_temp[ICD10GROUP != "C44",]$ICD10GROUP ="O&U"
				dt_temp[ICD10GROUP != "C44",]$ICD10GROUPLABEL ="Other and unspecified"
				dt_temp <- dt_temp[, .(CASES=sum(CASES)),by=.(ICD10GROUP, ICD10GROUPLABEL, YEAR,SEX, AGE_GROUP,AGE_GROUP_LABEL,COUNT,REFERENCE_COUNT) ]
					
				dt_temp <- canreg_year_cases_data(dt_temp, skin=bool_skin)
				
			}
				
		}
		else if (table_number == 9){

			if (!is.null(input$slideAgeRange)) {
			
				dt_temp  <- copy(dt_base)
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
			
				dt_temp  <- copy(dt_base)
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
			
				dt_temp  <- copy(dt_base)
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
		else if (table_number == 12){

			if (!is.null(input$selectRegistry1) & 
				!is.null(input$selectRegistry2) & 
				!is.null(input$selectRegistry3) ) {

				registry_selection <- 
					c(
					input$selectRegistry1,
					input$selectRegistry2,
					input$selectRegistry3,
					ls_args$header)  

				dt_temp  <- copy(dt_base)
				dt_temp <- dt_temp[ICD10GROUP != "C44",]
				dt_temp <- dt_temp[ICD10GROUP != "O&U",]
				dt_temp <- canreg_ageSpecific_rate_data(dt_temp, keep_ref = TRUE)

				dt_CI5_data <- canreg_import_CI5_data(dt_temp, paste0(script.basename, "/CI5_alldata.rds"))
				dt_temp <- shiny_merge_CI5_registry(dt_temp,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header, number=40 )
				
				dt_temp <- dt_temp[country_label %in% registry_selection, ]
				setkeyv(dt_temp, c("CSU_RANK", "SEX","asr"))
				dt_temp[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
				dt_temp[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
				cancer_list <- sort(as.character(unique(dt_temp$cancer_label)))
				cancer_selected <- as.character(unique(dt_temp[CSU_RANK == 1 & SEX == levels(SEX)[1],]$cancer_label))
				
				updateSelectInput(session, "selectCancerSite",choices =cancer_list,selected = cancer_selected)



			}
		}	
	}
 
  return(dt_temp)
  
}




shiny_plot <- function(dt_plot,input,session, download = FALSE,slide=FALSE, file = NULL) {
  
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
			table_number <- isolate(input$select_table)
			if (table_number != 12) {
				ls_args$header  <- ""
			}
			
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
			header = paste0(ls_args$header, "\n\n",i18n$t("All cancers but C44"))
			}
			else {
			header = paste0(ls_args$header, "\n\n",i18n$t("All cancers"))
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
					ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ", i18n$t("years old"))
					
					
				} 
				else if (isolate(input$radioValue) == "cases"){
					var_top <- "CASES"
					digit <- 0
					ytitle <-  paste0(i18n$t("Number of cases"),", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				}
				else if (isolate(input$radioValue) == "cum") {
					var_top <- "cum_risk"
					digit <- 2
					if (last_age >= 15) {
						age2 <- 74
					} else {
						age2 <- isolate(input$slideAgeRange)[2]-1
					}
					ytitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", 0-",age2, " ",i18n$t("years old"))
					
					
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
					ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				} 
				else if (isolate(input$radioValue) == "cases"){
					var_top <- "CASES"
					digit <- 0
					ytitle <-  paste0(i18n$t("Number of cases"),", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
					
					
				}
				else if (isolate(input$radioValue) == "cum") {
					var_top <- "cum_risk"
					digit <- 2
					if (last_age >= 15) {
						age2 <- 74
					} else {
						age2 <- isolate(input$slideAgeRange)[2]-1
					}
					ytitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", 0-",age2, " ",i18n$t("years old"))
					
					
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
						plot_title = ls_args$header,
						xtitle = i18n$t("Age at diagnosis"),
						ytitle = i18n$t("Age-specific incidence rate per")
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

		
		
		 if ( !is.null( input$slideNbTopBar) & !is.null(input$radioLog) ) {

		 	
				
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
				

				 ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
		 
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
				
				 ytitle <- paste0(i18n$t("Estimated annual percentage change")," (%), ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old"))
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
				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				
				last_age <- (isolate(input$slideAgeRange)[2]/5)
				max_age <- canreg_age_group$last_age+1 

				if (last_age < max_age) {
					age2 <- isolate(input$slideAgeRange)[2]-1
				} else {
					age2 <- paste0(((max_age-1)*5), "+")
				}
				
				ytitle <- paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", isolate(input$slideAgeRange)[1], "-", age2, " ",i18n$t("years old") )
		 
				
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
		else if (table_number == 12){
			
			if (!is.null( input$selectCancerSite) & !is.null(input$radioSex)) {

				dt_plot <- dt_plot[cancer_label == input$selectCancerSite,]
				dt_plot <- dt_plot[SEX == input$radioSex]

				if (nrow(dt_plot) == 0 ) {
					temp <- ifelse(input$radioSex == "Male", "Female", "Male")
					updateRadioButtons(session, "radioSex", selected= temp)
					return()
				}

				xtitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","))

				
				if (download) {
				 
					canreg_output(output_type = output_type, filename =file,landscape = TRUE,list_graph = FALSE,
								FUN=shiny_bar_CI5_compare,
								dt=dt_plot,
								xtitle = xtitle,
		                		text_size_factor=1.1)
							
				}
				else {
					
					shiny_bar_CI5_compare(
						dt=dt_plot,
						xtitle = xtitle,
                		text_size_factor=1.2)
								
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
		var_age        ="AGE_GROUP",
		var_cases      = "CASES",
		var_py         ="COUNT",
		group_by       = "SEX",
		plot_title     = ls_args$header,
		plot_subtitle  = plot_subtitle,
		color_trend    = color_trend,
		logscale       = logscale,
		age_label_list = unique(dt_plot[["AGE_GROUP_LABEL"]]),
		xtitle         = i18n$t("Age at diagnosis"),
		ytitle         = i18n$t("Age-specific incidence rate per"),
		label_group_by = c(i18n$t("Male"),i18n$t("Female"))
 		 )$csu_plot
		
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
                                plot_title="") 

{

  if (return_data) {
    dt <- dt[, c(group_by,var_year,var_asr), with=FALSE]
    setkeyv(dt, c("SEX",var_year))
    return(dt)
    stop() 
  }


   temp_level <- c(i18n$t(levels(dt_plot$SEX)[1]),i18n$t(levels(dt_plot$SEX)[2]))
   dt_plot$SEX <- factor(dt_plot$SEX,labels = temp_level) 

   color_trend <- c("#2c7bb6", "#b62ca1")
    
   plot <- Rcan:::core.csu_time_trend(dt_plot,
                                    var_trend = "asr",
                                    var_year = "YEAR",
                                    group_by = "SEX",
                                    logscale = logscale,
                                    smoothing = NULL,
                                    ytitle = ytitle,
									xtitle = i18n$t("Year"),
                                    plot_title = plot_title,
                                    color_trend = color_trend)$csu_plot


  print(plot)

}

#shiny function for CI5 comparison


shiny_bar_CI5_compare <- function(dt,group_by = "SEX", landscape = TRUE,list_graph=FALSE,
                                        xtitle = "",digit  =  1,text_size_factor =1.5,
                                        return_data  =  FALSE) 
{
  
  if (return_data) {
    setnames(dt, "CSU_RANK","cancer_rank")
    dt <-  dt[,-c("ICD10GROUPCOLOR"), with=FALSE]
    
    return(dt)
    stop() 
  }
  
  
  CI5_registries <- as.character(unique(dt$country_label))
  caption <- NULL
  if (any(grepl("\\*",CI5_registries))) {
    caption <- paste0("*: ",i18n$t("Regional registries"))
  }

  dt[["country_label"]] <-Rcan:::core.csu_legend_wrapper(dt[["country_label"]], 14)
  dt[,country_label:=factor(country_label, levels=country_label)]
      
  temp <- csu_bar_plot(dt=dt, 
                 var_top="asr",
                 var_bar="country_label",
                 plot_title = unique(dt$cancer_label),
                 plot_subtitle = unique(i18n$t(dt$SEX)), 
                 plot_caption = caption,
                 xtitle=xtitle,
                 digit = digit,
                 color_bar = as.character(dt$ICD10GROUPCOLOR),
                 text_size_factor = text_size_factor,
                 landscape = TRUE) 
 


      
  geom_text_index <- which(sapply(temp$layers, function(x) class(x$geom)[1]) == "GeomText")
  temp$layers[[geom_text_index]]$aes_params$size <- 6 
  geom_hline_index <- which(sapply(temp$layers, function(x) class(x$geom)[1]) == "GeomHline")
  temp$layers[[geom_hline_index]]$aes_params$size <- 0.4
      
  print(temp)
  
}


shiny_merge_CI5_registry <- function(dt, dt_CI5, registry_region, registry_label, number=5) {
  
  ##calcul of ASR for canreg
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX","ICD10GROUP","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt),
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

  if (exists("pb")) {
    close(pb)
  }
  
  shiny_log <- file(log_file,open="wt")
  sink(shiny_log)
  sink(shiny_log, type="message")
  
  #print error
  cat("This file contains the data and parameter of this canreg5 R-shiny application.\n") 
  cat(paste0("If an error occured, please restart this application send this log file: `",filename,"` to canreg@iarc.fr, with a description of the error\n"))
  cat("The second part of this log (After '----------------------') contains your aggregated data, if you do not won't to share the aggregated data, you can delete this part.\n\n")
  cat("\n")
  
  #print argument from canreg
  print(ls_args)
  cat("\n")
  
  #print environment
  ##print(ls.str())
  ##cat("\n")
	
	#print R version and package load
  print(sessionInfo())
  cat("\n")

  #print missing package
  packages_list <- c("data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png","shiny.i18n", "Rcan")

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
  close(shiny_log)
  
}


shiny_export_data <- function(log_file) {
  
  shiny_log <- file(log_file,open="wt")
  sink(shiny_log)
  sink(shiny_log, type="message")
  
  #print argument from canreg
  cat("arguments\n")
  dput(ls_args)
  cat("\n")
  cat("data file\n")

  
  
  dput(as.data.frame(dt_base))
  cat("\n")
  cat("basis file\n")
  dput(as.data.frame(dt_basis))

  #close log_file and send to canreg
  sink(type="message")
  sink()
  close(shiny_log)
  
}


import_shiny_date <- function(datafile) {

	fileTemp1 <- paste0(tempdir(),"/tempargs.txt")
	fileTemp2 <- paste0(tempdir(),"/tempdata.txt")
	fileTemp3 <- paste0(tempdir(),"/tempbasis.txt")

	con_args=file(fileTemp1,open="wt")
	sink(con_args)
	sink(con_args, type="message")


	con_source=file(datafile,open="r")
	content=readLines(con_source)

	j<-2
	args <- NULL
	while (content[j] != "data file") {
		cat(content[j])
		j <- j+1
		if (j == length(content)-1){
			sink(type="message")
			sink()
			close(con_args)
			close(con_source)
			return(NULL)
		}
	}

	sink(type="message")
	sink()
	close(con_args)
	ls_args <-dget(paste0(tempdir(),"/tempargs.txt"))

	con_data=file(fileTemp2,open="wt")
	sink(con_data)
	sink(con_data, type="message")
	j<-j+1

	while (content[j] != "basis file") {
		cat(content[j])
		j <- j+1
	}


	sink(type="message")
	sink()
	close(con_data)

	dt_base <-as.data.table(dget(paste0(tempdir(),"/tempdata.txt")))

	con_basis=file(fileTemp3,open="wt")
	sink(con_basis)
	sink(con_basis, type="message")

	for (i in (j+1):length(content)) {
		cat(content[i])
	}

	sink(type="message")
	sink()
	close(con_basis)

	dt_basis <-as.data.table(dget(paste0(tempdir(),"/tempbasis.txt")))

	close(con_source)

	
	return(list(ls_args = ls_args, dt_base = dt_base,dt_basis = dt_basis))

}

shiny_dwn_data <- function(log_file) {

	dt_temp <- copy(dt_base)
	dt_temp[, ICD10GROUPLABEL := NULL]
	dt_temp[, ICD10GROUPCOLOR := NULL]
	dt_temp[, AGE_GROUP := NULL]
	
	write.csv(dt_temp, paste0(log_file),row.names = FALSE)

}

shiny_dwn_report <- function(log_file, directory_path, ann) {

	

	if (is.na(directory_path)) {
		ls_args$out <- tempdir()
	}
	else {
		ls_args$out <- directory_path
	}

	


	#check if report path exist (if not create report path)
	path <- ls_args$out
	if (ls_args$sc=="null") {
	report_path <- paste0(path, "/report-template")
	} else {
	report_path <- paste0(path, "/report-template-", ls_args$sc)
	}
	if(!file_test("-d",report_path)) {
	dir.create(report_path)
	}



	incProgress(0, detail = "create docx")

	doc <- read_docx(paste(sep="/", script.basename,"slide_template", "template.docx"))
	doc <- rcan_report(doc, report_path, dt_base , ls_args,ann=ann, shiny=TRUE )
	
  print(doc, log_file)
  


}

shiny_dwn_slide <- function(log_file, ann) {


	ls_args$out <- tempdir()

	incProgress(0, detail = "create docx")

	doc <- read_pptx(path=paste(sep="/", script.basename,"slide_template", "canreg_template.pptx"))
	doc <- rcan_slide(doc, dt_base , ls_args, ann=ann, shiny=TRUE)
	
  print(doc, log_file)
  


}

shiny_update_dwn_folder <- function(output,values) {

	download_dir <<- choose.dir(download_dir)
	if (is.na(download_dir)) {
		output$directorypath <- renderText({"Please select a folder"})
	}
	else {
		output$directorypath <- renderText({download_dir})
	}
	shiny_list_folder_content(output)

}

shiny_list_folder_content <- function(output) {

	path <- download_dir

	if (is.na(path)) {
		output$reportHTML <- renderUI({shiny_report_info(path, TRUE)})
	}
	else {
		
		if (ls_args$sc=="null") {
			report_source <- paste0(path, "\\report-template")
		} else {
			report_source <- paste0(path, "\\report-template-", ls_args$sc)
		}

		if(!file_test("-d",report_source)) {
			temp_path <- report_source
			output$reportHTML <- renderUI({shiny_report_info(temp_path, TRUE)})
			report_source <- paste0(script.basename,"/report_text")
		}
		else {
			output$reportHTML <- renderUI({shiny_report_info(report_source, FALSE)})
		}

		#temp <- as.data.frame(list.files(report_source))
		#names(temp) <-"Template files" 
	  #output$folderContent <- renderTable(temp)

	}

}

shiny_report_info <- function (path, new=TRUE) {
	
	if (is.na(path)) {
		text <- tags$p("There is no folder selected, the template files cannot be modified")
	}
	else {

		if (new) {
			text <- tags$p(
				"There is no prior report in this folder",tags$br(),tags$br(),
				"The template files will be create from base, and can be edit later in the folder:",tags$br(),
				path)
		}
		else {
			text <- tags$p(
				"The report will be based on the template files in the folder:",tags$br(),
				path)
		}
	}

	return(text)
}

