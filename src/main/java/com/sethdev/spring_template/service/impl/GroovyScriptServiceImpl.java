package com.sethdev.spring_template.service.impl;

import com.sethdev.spring_template.models.sys.dialog.DropdownItem;
import com.sethdev.spring_template.service.GroovyScriptService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroovyScriptServiceImpl implements GroovyScriptService {

    public Binding getBinding(Map<String, Object> data) {
        Binding binding = new Binding();
        binding.setVariable("data", data);
        return binding;
    }

    public Binding getDefaultBinding() {
        Binding binding = new Binding();
        return binding;
    }

    @Override
    public Boolean runForCustomDialogDisabledScript(String script, Map<String, Object> data) throws Exception {
        // Create a GroovyShell instance and evaluate the script
        GroovyShell shell = new GroovyShell();
        Script scriptObject = shell.parse(script);

        // Bind variables to the script using a Binding instance
        Binding binding = this.getBinding(data);
        scriptObject.setBinding(binding);

        // Evaluate the script again with the binding
        return (boolean) scriptObject.run();
    }

    /** Used to execute the script of columns in a custom dialog with type `SCRIPT` */
    @Override
    public Object runForCustomDialogScriptColumn(String script, Map<String, Object> data) {
        try {
            // Create a GroovyShell instance and evaluate the script
            GroovyShell shell = new GroovyShell();
            Script scriptObject = shell.parse(script);

            // Bind variables to the script using a Binding instance
            Binding binding = this.getBinding(data);
            scriptObject.setBinding(binding);

            // Evaluate the script again with the binding
            return scriptObject.run();
        } catch (CompilationFailedException e) {
            return null;
        }
    }

    /** Used to execute the script of columns with search field type `SCRIPT` */
    @Override
    public List<DropdownItem> runForCustomDialogSearchFieldScriptColumn(String script,
                                                                        Map<String, Object> filterParams,
                                                                        Map<String, Object> searchParams) {
        try {
            Binding binding = this.getDefaultBinding();
            binding.setVariable("filterParams", filterParams);
            binding.setVariable("searchParams", searchParams);
            GroovyShell shell = new GroovyShell(binding);
            Object result = shell.evaluate(script);
            return result != null ? (List<DropdownItem>) result : null;
        } catch (CompilationFailedException e) {
            return new ArrayList<>();
        }
    }
}
