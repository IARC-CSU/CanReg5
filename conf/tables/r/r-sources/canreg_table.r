## version 1.10

canreg_table_shiny <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png" ,"shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)

  shiny_dir <- paste(sep="/", script.basename, "shiny")
  source(paste(sep="/", script.basename, "r-sources", "shiny_core.r"))
  runApp(appDir =shiny_dir, launch.browser =TRUE)


  #talk to canreg
  cat(paste("-outFile",ls_args$filename,sep=":"))
  
}

canreg_table_report <- function(ls_args)

{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)

  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )

  dt_basis <<- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )

  dt_iccc <<- canreg_merge_iccc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICCC",  "YEAR", "SEX")
  )

  dt_pyramid <<- canreg_pop_data(pop_file =ls_args$pop)

  graph_width <<- 6

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

  sysName <<- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <<- winProgressBar(
      title = "Create docx",
      label = "Initializing"
    )
  } 

  doc <- read_docx(paste(sep="/", script.basename,"slide_template", "template.docx"))
  
  
  doc <- canreg_report(doc, report_path, dt_all, ls_args)
  
  print(doc, ls_args$filename)

  if (sysName == "Windows") {
    close(pb)
  }

  cat(paste("-outFile",ls_args$filename,sep=":"))


}

canreg_table_child_table <- function(ls_args)
{
  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)

  #get ICCC data
  dt_iccc <<- canreg_merge_iccc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICCC",  "YEAR", "SEX")
  )

  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_child_table,
                df_data=dt_iccc,
                canreg_header = ls_args$header)

  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename)


}

canreg_table_slide <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","officer","flextable", "zip", "bmp", "jpeg", "png","shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )

  dt_basis <<- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
  )

  dt_iccc <<- canreg_merge_iccc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICCC",  "YEAR", "SEX")
  )


  dt_pyramid <<- canreg_pop_data(pop_file =ls_args$pop)

  sysName <<- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <<- winProgressBar(
      title = "Create pptx",
      label = "Initializing"
    )
  } 

  doc <- read_pptx(path=paste(sep="/", script.basename,"slide_template", "canreg_template.pptx"))
  
  #slide.layouts(doc)
  
  doc <- canreg_slide(doc, dt_all, ls_args)
  
  
  print(doc, target = ls_args$filename)

  if (sysName == "Windows") {
    close(pb)
  }

#talk to canreg
  cat(paste("-outFile",ls_args$filename,sep=":"))

}

canreg_table_population_pyramid <- function(ls_args)
{



  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #Prepare canreg data population pyramid
  dt_all <- canreg_pop_data(pop_file =ls_args$pop)

  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_population_pyramid,
                df_data=dt_all,
                canreg_header = ls_args$header)

  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename)


}

canreg_table_cases_age_bar <- function(ls_args)
{

  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  

  #Prepare canreg data for count per sex and age group
  dt_all <- canreg_age_cases_data(dt_all, skin=ls_args$skin)
  
  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_cases_age_bar,
                df_data=dt_all,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                canreg_header = ls_args$header,
                skin=ls_args$skin)


   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=FALSE)


}

canreg_table_cases_age_pie <- function(ls_args)
{

  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )

  #Prepare canreg data for count per sex and age group
  dt_all <- canreg_age_cases_data(dt_all, skin=ls_args$skin)
  
  #update header
  if (!ls_args$skin) {
    header = paste0(ls_args$header, "\n\n", i18n$t("All cancers"))
  } else {
    header = paste0(ls_args$header, "\n\n",i18n$t("All cancers but C44"))
  }
  
  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_age_cases_pie_multi_plot,
                dt=dt_all,
                canreg_header = header)

   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=FALSE)



}

canreg_table_cases_year_bar <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )

  #Prepare canreg data for count per sex and age group
  dt_all <- canreg_year_cases_data(dt_all, skin=ls_args$skin)
  
  
  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_cases_year_bar,
                dt=dt_all,
                skin=ls_args$skin,
                canreg_header = ls_args$header)

   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=FALSE)

}

canreg_table_age_specific_rate_top <- function(ls_args)
{

   #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  ##Prepare canreg data for ageSpecific rate

  dt_all <- dt_all[ICD10GROUP != "C44",]
  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt_all <- canreg_ageSpecific_rate_data(dt_all)


  
  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = FALSE,list_graph = TRUE,
                FUN=canreg_ageSpecific_rate_top,
                df_data=dt_all,logscale = ls_args$logr,nb_top = ls_args$number,
          plot_title = ls_args$header)

     #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=TRUE)



}

canreg_table_age_specific_rate_site <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  ##Prepare canreg data for ageSpecific rate
  dt_all <- canreg_ageSpecific_rate_data(dt_all)
  
  
  ##Produce output
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = FALSE,
                list_graph = TRUE,
                FUN=canreg_ageSpecific_rate_multi_plot,dt=dt_all,group_by="SEX",var_age_label_list = "AGE_GROUP_LABEL",
                logscale = ls_args$logr,  
                color_trend=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                multi_graph= ls_args$multi_graph,
          canreg_header=ls_args$header)
  
  
  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=TRUE)

}

canreg_table_time_trend <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg", "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  
  year_info <<- canreg_get_years(dt_all)
  if (year_info$span < 2) {
    stop(i18n$t("Time trend analysis need at least 2 years data"))
  }

  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
  
  ## get age group label
  
  canreg_age_group <- canreg_get_agegroup_label(dt, ls_args$agegroup)
  
  ##calcul of ASR
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX", "YEAR", "ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                    first_age = canreg_age_group$first_age+1+1,
                    last_age= canreg_age_group$last_age+1+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  
  #produce graph
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = TRUE,
                FUN=canreg_asr_trend_top,
                dt=dt,number = ls_args$number,
                canreg_header = ls_args$header,
                ytitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))
  
  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=TRUE)

}



canreg_table_ASR_bar_top <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg","shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)


  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  ##Prepare canreg data for ageSpecific rate
  dt_all <- dt_all[ICD10GROUP != "C44",]
  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
  
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ##calcul of ASR
  dt_all<- Rcan:::core.csu_asr(df_data =dt_all, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
               group_by = c("cancer_label", "SEX"), missing_age = canreg_missing_age(dt_all),
               first_age = canreg_age_group$first_age+1,
               last_age= canreg_age_group$last_age+1,
               pop_base_count = "REFERENCE_COUNT",
               age_label_list = "AGE_GROUP_LABEL")
    
  #produce graph
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
              FUN=canreg_bar_top,
              df_data=dt_all,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),nb_top = ls_args$number,
        canreg_header = ls_args$header,
        ytitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label))

  #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=FALSE)
  
}

canreg_table_ASR_bar_top_oneside <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg",  "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  dt_all <- dt_all[ICD10GROUP != "C44",]
  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = FALSE)
  
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ##calcul of ASR
  dt_asr<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                        group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                        first_age = canreg_age_group$first_age+1,
                        last_age= canreg_age_group$last_age+1,
                        pop_base_count = "REFERENCE_COUNT",
                        age_label_list = "AGE_GROUP_LABEL")
  
  ##calcul of cumulative risk
  dt_cum_risk <- canreg_cum_risk_core(df_data = dt,var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                                   group_by = c("cancer_label", "SEX","ICD10GROUPCOLOR"), missing_age = canreg_missing_age(dt_all),
                                   last_age= canreg_age_group$last_age+1,
                                   age_label_list = "AGE_GROUP_LABEL")
  
  dt_bar <- dt_asr

  
  if (ls_args$data=="CASES") {
    var_top <- "CASES"
    digit <- 0
    xtitle <- paste0( i18n$t("Number of cases"),", ", canreg_age_group$label)
  } else if (ls_args$data=="ASR") {
    var_top <- "asr"
    digit <- 1
    xtitle<-paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=","), ", ", canreg_age_group$label)
  } else if (ls_args$data=="CR") {
    var_top <- "cum_risk"
    digit <- 2
  temp_agegroup <- paste0("0-", canreg_age_group$last_age)
    canreg_age_group <- canreg_get_agegroup_label(dt,temp_agegroup)
    dt_bar <- dt_cum_risk
    xtitle<-paste0(i18n$t("Cumulative incidence risk (percent)"),", ", canreg_age_group$label)
    
  }
  
  
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = FALSE,list_graph = TRUE,
                FUN=canreg_bar_top_single,
                dt=dt_bar,var_top=var_top,nb_top = ls_args$number,
                canreg_header = ls_args$header,digit=digit,
                xtitle=xtitle)
  
     #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=TRUE)


}

canreg_table_EAPC_scatter <- function(ls_args)
{

   #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg",  "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  year_info <<- canreg_get_years(dt_all)
  if (year_info$span < 3) {
    stop("EAPC analysis need at least 3 years data")
  }


  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
 
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ##calcul of ASR
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_all),
                    first_age = canreg_age_group$first_age+1,
                    last_age= canreg_age_group$last_age+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  
  ##Keep top based on rank
  dt <- Rcan:::core.csu_dt_rank(dt,
                    var_value= "CASES",
                    var_rank = "cancer_label",
                    group_by = "SEX",
                    number = ls_args$number
  )
  
  
  ##calcul eapc
  dt_eapc <- Rcan:::core.csu_eapc(dt, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX","CSU_RANK"))
  dt_eapc <-as.data.table(dt_eapc)
  
  
  ##produce graph
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = FALSE,
                FUN=canreg_eapc_scatter,
                dt_plot=dt_eapc,color_bar=c("Male" = "#2c7bb6", "Female" = "#b62ca1"),
                canreg_header = ls_args$header,
                ytitle=paste0(i18n$t("Estimated annual percentage change")," (%), ", canreg_age_group$label))

  
  
   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=FALSE)





}

canreg_table_EAPC_scatter_CI <- function(ls_args)
{

   #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg",  "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  year_info <<- canreg_get_years(dt_all)
  if (year_info$span < 3) {
    stop("EAPC analysis need at least 3 years data")
  }
  
  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE, keep_year = TRUE)
  first_age <- as.numeric(substr(ls_args$agegroup,1,regexpr("-", ls_args$agegroup)[1]-1))
  last_age <- as.numeric(substr(ls_args$agegroup,regexpr("-", ls_args$agegroup)[1]+1,nchar(ls_args$agegroup)))
  
  ## get age group label
  canreg_age_group <- canreg_get_agegroup_label(dt_all, ls_args$agegroup)
  
  ##calcul of ASR
  dt<- Rcan:::core.csu_asr(df_data =dt, var_age ="AGE_GROUP",var_cases = "CASES", var_py = "COUNT",
                    group_by = c("cancer_label", "SEX", "YEAR"), missing_age = canreg_missing_age(dt_all),
                    first_age = canreg_age_group$first_age+1,
                    last_age= canreg_age_group$last_age+1,
                    pop_base_count = "REFERENCE_COUNT",
                    age_label_list = "AGE_GROUP_LABEL")
  dt <- as.data.table(dt)
  
  
  ##Keep top based on rank
  dt <- Rcan:::core.csu_dt_rank(dt,
                    var_value= "CASES",
                    var_rank = "cancer_label",
                    group_by = "SEX",
                    number = ls_args$number
  )
  
  
  ##calcul eapc
  dt_eapc <- Rcan:::core.csu_eapc(dt, var_rate = "asr",var_year = "YEAR" ,group_by =c("cancer_label", "SEX","CSU_RANK"))
  dt_eapc <-as.data.table(dt_eapc)
  
  
  
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = TRUE,
                FUN=canreg_eapc_scatter_error_bar,
                dt=dt_eapc,
                canreg_header = ls_args$header,
                ytitle=paste0(i18n$t("Estimated annual percentage change")," (%), ", canreg_age_group$label))

  
   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, sex_graph=TRUE)

}


canreg_table_CI5_comp <- function(ls_args)
{

  #load dependency packages
  canreg_load_packages(c("data.table", "ggplot2", "gridExtra", "scales", "Cairo","bmp", "jpeg",  "shiny.i18n", "Rcan"))
  i18n <<- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
  
  i18n$set_translation_language(ls_args$lang)
  
  #merge incidence and population
  dt_all <- canreg_merge_inc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
    column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
  )
  
  region_admit <<- c("EastMed", "Americas", "West Pacific", "Europe", "SEAsia", "Africa")
  
  if (!ls_args$sr %in% region_admit) {
    stop(paste0("the actual registry region (", ls_args$sr, ") is not in the CI5 region defined list.\nPlease edit the region in Canreg (tools -> Databse structure) before running this table."))
  }
  
  ##Prepare canreg data for ageSpecific rate
  dt_all <- dt_all[ICD10GROUP != "C44",]
  dt_all <- dt_all[ICD10GROUP != "O&U",]
  dt_all <- canreg_ageSpecific_rate_data(dt_all, keep_ref = TRUE)
  
  # import CI5 data with same cancer code and same age group
  dt_CI5_data <- canreg_import_CI5_data(dt_all, paste0(script.basename, "/r-sources", "/CI5_data.rds"))
  
  #merge CI5 and canreg data
  dt_both <- canreg_merge_CI5_registry(dt_all,dt_CI5_data, registry_region = ls_args$sr, registry_label = ls_args$header, number=ls_args$number )
  
  #create bar chart color graphique 
  setkeyv(dt_both, c("CSU_RANK", "SEX","asr"))
  dt_both[country_label!=ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"6E")]
  dt_both[country_label==ls_args$header,ICD10GROUPCOLOR:=paste0(ICD10GROUPCOLOR,"FF")]
  
  
  canreg_output(output_type = ls_args$ft, filename = ls_args$out,landscape = ls_args$landscape,list_graph = TRUE,
                FUN=canreg_bar_CI5_compare,
                dt=dt_both,xtitle=paste0(i18n$t("Age-standardized incidence rate per")," ", formatC(100000, format="d", big.mark=",")),
                number=ls_args$number,text_size_factor=1,multi_graph=TRUE)
  
  
  
   #talk to canreg
  canreg_output_cat(ls_args$ft, ls_args$filename, list_graph=TRUE)



}


canreg_clean_install <- function(args)
{ 
  ## 2019-07-02(ME): This list now contains packages from both analysis and format checking... (starting with XML)
  packages_list <- c("Rcpp", "data.table", "ggplot2","shiny","shinydashboard", "shinyjs","gridExtra", "scales", "Cairo","grid","officer","flextable", "zip", "bmp", "jpeg", "png", "shiny.i18n","Rcan", "XML", "plyr", "stringr", "dplyr", "RJSONIO", "jsonlite", "anchors", "lubridate", "reshape2", "tidyr")

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

  ft <- "txt"

  #create filename from out and avoid double extension (.pdf.pdf)
  if (substr(out,nchar(out)-nchar(ft),nchar(out)) == paste0(".", ft)) {
    filename <<- out
    out <- substr(out,1,nchar(out)-nchar(ft)-1)
  } else {
    filename <<- paste(out, ft, sep = "." )
  }


  log_connection <<- file(filename,open="wt")

  sink(log_connection)
  sink(log_connection, type="message")

    
  options(warn = 1)
  sysName <- Sys.info()[['sysname']]

  if (sysName == "Windows") {
    pb <- winProgressBar(
    title = "Download all R packages",
    label = "Initializing"
    )
  } 

    


  cat("This log file contains warnings, errors, and package availability information\n\n")

  if (getRversion() == '3.2.0') {
    
    stop("The table builder do not work with R '3.2.0', please install any version after '3.2.1'.\n '3.2.1' would do as well as '3.3.0' for instance.\n You can edit the Path in the 'Option' in CanReg.") 
    
  }



  #managing installing package for old R version.
  if (getRversion() < '3.2.0') {
    utils::setInternet2(TRUE)
    if (sysName == "Windows") {
      options(download.file.method = "internal")
    } else if (sysName == "Linux") {
      options(download.file.method = "wget")
    } else if (sysName == "Darwin") {
      options(download.file.method = "curl")
    }
  } else if (getRversion() < '3.3.0') {
    if (sysName == "Windows") {
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


  ap <- available.packages()

  for (i in c(packages_list, "gtools")) {

    bool_internet <- FALSE
    if (i %in% rownames(ap)) {
      bool_internet <- TRUE
    }
  }

  if (!bool_internet) {
    stop("CanReg5 can't access the internet and download the R packages. Please try again later.") 
  }


  if (sysName == "Windows") {
    setWinProgressBar(pb, 0,label = "Cleaning CanReg R folder")
  }
  unlink(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
  dir.create(file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")),recursive = TRUE)
  .libPaths(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5"))


  cat("This is the actual repository\n")
  print(getOption("repos"))

  for (i in c(packages_list, "gtools")) {
    print(paste0(i," available: ", i %in% rownames(ap)))
    
  }

  #to avoid package from source which need compilation.
  if (sysName == "Windows") {
    options(pkgType="win.binary") 
  }

  if (sysName == "Windows") {
    setWinProgressBar(pb, 0,label = "Downloading gtools")
  }

  install.packages("gtools", dependencies=  c("Depends", "Imports", "LinkingTo"), quiet = TRUE)
  require("gtools", character.only = TRUE)

  all_pck <- getDependencies(packages_list, installed=FALSE, available=TRUE)
  missing_packages <- c(packages_list, all_pck)

  for (i in seq_along(missing_packages)) {

    if (sysName == "Windows") {
      setWinProgressBar(
        pb, 
        value = i / (length(missing_packages) + 1),
        label = sprintf("%s (This might take a while...)", missing_packages[i])
      )
    }

    install.packages(missing_packages[i], dependencies=  FALSE, quiet = TRUE)

  }

  if (sysName == "Windows") {
    setWinProgressBar(pb, 0, title = "Loading R packages",label = "Initializing")
  }

  packages_list <- c(packages_list,"grid")

  for (i in seq_along(packages_list)) {

    if (sysName == "Windows") {
      setWinProgressBar(
        pb, 
        value = i / (length(packages_list) + 1),
        label = sprintf("Loading package - %s", packages_list[i])
      )
    }

    require(packages_list[i], character.only = TRUE)

  }


  if (sysName == "Windows") {
    setWinProgressBar(pb, 1, title = "Loading R packages",label = "Done")
    close(pb)
  }





  sink(type="message")
  sink()
  close(log_connection)
  cat(paste("-outFile",filename,sep=":"))




}
