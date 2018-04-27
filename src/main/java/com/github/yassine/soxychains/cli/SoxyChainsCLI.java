package com.github.yassine.soxychains.cli;

import com.github.yassine.soxychains.ConfigurationModule;
import com.github.yassine.soxychains.SoxyChainsModule;
import com.github.yassine.soxychains.cli.command.CommandGroup;
import com.github.yassine.soxychains.cli.command.ConfigurableCommand;
import com.github.yassine.soxychains.cli.command.RequiresExtraModule;
import com.google.common.collect.*;
import com.google.common.reflect.ClassPath;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.machinezoo.noexception.Exceptions;
import io.airlift.airline.Cli;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class SoxyChainsCLI {

  private final Cli<Runnable> cli;
  private final Set<Module> modules;

  @SneakyThrows
  public void run(String... args){
    Runnable runnable = cli.parse(args);
    ArrayList<Module> ms = new ArrayList<>(this.modules);
    if(runnable instanceof ConfigurableCommand){
      String configPath = ((ConfigurableCommand) runnable).getConfigPath();
      FileInputStream fis = new FileInputStream(new File(configPath));
      ms.add(new ConfigurationModule(fis));
      ms.add(new SoxyChainsModule());
      if(runnable.getClass().isAnnotationPresent(RequiresExtraModule.class)){
        stream(runnable.getClass().getAnnotation(RequiresExtraModule.class).value())
          .map(clazz -> Exceptions.sneak().get(clazz::newInstance))
          .forEach(ms::add);
      }
      Injector injector = Guice.createInjector(ms);
      injector.injectMembers(runnable);
    }
    runnable.run();
  }

  static class Builder {

    private Package cliCommandsPackage;
    private Set<Module> modules = new HashSet<>();

    public Builder withCommandsPackage(Package p ){
      cliCommandsPackage = p;
      return this;
    }

    public Builder withModules(Module... modules){
      this.modules.addAll(Arrays.asList(modules));
      return this;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public SoxyChainsCLI build(){
      ClassPath classPath = ClassPath.from(Application.class.getClassLoader());
      Set<Package> packages = classPath.getTopLevelClassesRecursive(cliCommandsPackage.getName())
        .stream()
        .filter(c -> c.getSimpleName().equals("package-info"))
        .map(c -> c.load().getPackage())
        .filter(pkg -> pkg.isAnnotationPresent(CommandGroup.class))
        .collect(Collectors.toSet());

      ImmutableMap<String, Package> packageIndex = Maps.uniqueIndex(packages, Package::getName);
      ImmutableSet.Builder<Class> builder = ImmutableSet.builder();
      FastClasspathScanner scanner = new FastClasspathScanner(cliCommandsPackage.getName());
      scanner.matchClassesWithAnnotation(Command.class, builder::add);
      scanner.scan();

      ImmutableMultimap<String,Class> commandIndex = Multimaps.index(builder.build(), command -> command.getPackage().getName());

      Cli.CliBuilder<Runnable> cliBuilder = new Cli.CliBuilder<Runnable>("soxy-chains")
        .withDescription("A scalable multi-layer TCP traffic tunneling application")
        .withDefaultCommand(Help.class)
        .withCommands(Help.class);
      commandIndex.keySet()
        .forEach(key -> {
          Package p = packageIndex.get(key);
          if(p != null){
            CommandGroup g = p.getAnnotation(CommandGroup.class);
            if(g.name().equals("default")){
              cliBuilder.withCommands((Iterable) commandIndex.get(key));
            }else{
              cliBuilder.withGroup(g.name())
                .withDescription(g.description())
                .withDefaultCommand(g.defaultCommand())
                .withCommands(commandIndex.get(key));
            }
          }
        });

      return new SoxyChainsCLI(cliBuilder.build(), modules);

    }
  }
}
