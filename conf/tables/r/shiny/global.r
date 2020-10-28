## version : 1.0

ls_args <- canreg_args(Args)


i18n <- Translator$new(translation_csvs_path  = (paste(sep="/", script.basename, "r-translations")))
i18n$set_translation_language(ls_args$lang)


dt_base <- canreg_merge_inc_pop(
	inc_file =ls_args$inc,
	pop_file =ls_args$pop,
	group_by = c("ICD10GROUP", "ICD10GROUPLABEL","ICD10GROUPCOLOR", "YEAR", "SEX"),
	column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL", "ICD10GROUPCOLOR"))
)

dt_basis <- canreg_merge_inc_pop(
  inc_file =ls_args$inc,
  pop_file =ls_args$pop,
  group_by = c("ICD10GROUP", "ICD10GROUPLABEL", "YEAR", "SEX", "BASIS"),
  column_group_list =list(c("ICD10GROUP", "ICD10GROUPLABEL"))
)

dt_iccc <- canreg_merge_iccc_pop(
    inc_file =ls_args$inc,
    pop_file =ls_args$pop,
    group_by = c("ICCC",  "YEAR", "SEX")
)

dt_pyramid <- canreg_pop_data(pop_file =ls_args$pop)
  
canreg_age_group <- canreg_get_agegroup_label(dt_base, ls_args$agegroup)
year_info <- canreg_get_years(dt_base)

dt_CI5_list <- readRDS(paste0(script.basename,"/r-sources", "/CI5_alldata.rds"))
dt_CI5_label <- as.character(unique(dt_CI5_list[cr == ls_args$sr, c("country_label"), with=FALSE])$country_label)

time_limit <- 9
graph_width <- 6
dim_width <- 2.7

download_dir <-  file.path(Sys.getenv("USERPROFILE"),"Documents",fsep="\\")

