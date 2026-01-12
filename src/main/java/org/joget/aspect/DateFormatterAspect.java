package org.joget.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.text.SimpleDateFormat;
import java.time.chrono.ThaiBuddhistChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.http.HttpServletRequest;

import org.joget.plugin.enterprise.DateFormatter;
import org.joget.workflow.util.WorkflowUtil;
import org.joget.apps.datalist.model.DataList;
// import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.lib.DatePicker;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.joget.commons.util.LogUtil;
import jakarta.servlet.RequestDispatcher;

@Aspect
public class DateFormatterAspect {

    @Pointcut("execution(public String org.joget.plugin.enterprise.DateFormatter.format(..))")
    public void format() {}

    @Around("format()")
    public Object aroundFormat(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().replacePath(null).toUriString();
        String uri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        String queryString = request.getQueryString();
        String fullUrl = baseUrl;

        if (uri != null) {
            fullUrl += uri;
        }

        if (queryString != null) {
            fullUrl += "?" + queryString;
        }

        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        
        // get params: [dataList, column, row, value]
        Object[] args = pjp.getArgs();
        DataList dataList = (DataList) args[0];
        // DataListColumn column = (DataListColumn) args[1];
        HashMap row = (HashMap) args[2];
        String value = (String) args[3];

        String result = "";
        String dataFormatString = "";
        String displayFormatString = "";

        if (value != null && !value.isEmpty()) {
            result = value;
            try {
                DateFormatter plugin = (DateFormatter) pjp.getTarget();
                Locale userLocale = LocaleContextHolder.getLocale();
                dataFormatString = plugin.getPropertyString("dataFormat");

                // getFormat() is not public
                java.lang.reflect.Method getFormatMethod = DateFormatter.class.getDeclaredMethod("getFormat");
                getFormatMethod.setAccessible(true);
                displayFormatString = (String) getFormatMethod.invoke(plugin);

                if ("th".equals(userLocale.getLanguage()) && "TH".equals(userLocale.getCountry()) && !"true".equalsIgnoreCase(plugin.getPropertyString("dateStoreInUTC"))) {
                    DateTimeFormatter inputFormatter = new DateTimeFormatterBuilder()
                            .parseCaseInsensitive()
                            .appendPattern(dataFormatString)
                            .toFormatter(Locale.ENGLISH)
                            .withChronology(ThaiBuddhistChronology.INSTANCE);
                    TemporalAccessor parsed = inputFormatter.parse(result);
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(displayFormatString, Locale.ENGLISH)
                            .withChronology(ThaiBuddhistChronology.INSTANCE);
                    result = outputFormatter.format(parsed);
                } else {
                    if ("true".equalsIgnoreCase(plugin.getPropertyString("dateStoreInUTC"))) {
                        dataFormatString = DatePicker.UTC_DATEFORMAT;
                    }
                    SimpleDateFormat dataFormat = new SimpleDateFormat(dataFormatString, userLocale);
                    SimpleDateFormat displayFormat = new SimpleDateFormat(displayFormatString, userLocale);
                    if ("true".equalsIgnoreCase(plugin.getPropertyString("dateStoreInUTC"))) {
                        dataFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        displayFormat.setTimeZone(LocaleContextHolder.getTimeZone());
                    }
                    Date date = dataFormat.parse(result);
                    result = displayFormat.format(date);
                }
            } catch (Exception e) {
                LogUtil.error("DateFormatterAspect", e, "appId=" + appDef.getAppId() + ", appVersion=" + appDef.getVersion() + ", dataList.id=" + dataList.getId() + ", " + row + ", dataFormat=" + dataFormatString + ", displayFormat=" + displayFormatString + ", fullUrl=" + fullUrl);
            }
        }
        
        return result;
    }
}