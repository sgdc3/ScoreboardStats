package com.github.games647.scoreboardstats.commands;

import com.github.games647.scoreboardstats.ScoreboardStats;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;

/**
 * This class forward all commands to the user commands for a better access
 */
public class SidebarCommands implements TabExecutor {

    private final ScoreboardStats plugin;

    private final Map<String, CommandHandler> commands = Maps.newHashMap();
    private final List<String> subCommands = Lists.newArrayList();

    public SidebarCommands(ScoreboardStats plugin) {
        this.plugin = plugin;

        registerCommands();
    }

    public Collection<CommandHandler> getHandlers() {
        return commands.values();
    }

    public CommandHandler getHandler(String subCommand) {
        return commands.get(subCommand);
    }

    public List<String> getSubCommands() {
        return Collections.unmodifiableList(subCommands);
    }

    /**
     * Executes the given command, returning its success
     *
     * @param sender Source of the command
     * @param cmd Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String subCommand = "toggle";
        String[] newArgs = args;
        if (args.length != 0) {
            subCommand = args[0];
            newArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        final CommandHandler commandHandler = commands.get(subCommand);
        if (commandHandler == null) {
            sender.sendMessage("Command not found");
        } else {
            if (commandHandler.hasPermission(sender)) {
                commandHandler.onCommand(sender, subCommand, newArgs);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) {
            return ImmutableList.of();
        }

        final String lastWord = args[args.length - 1];
        List<String> suggestion;
        if (args.length == 1) {
            suggestion = Lists.newArrayList();
            for (String subCommand : subCommands) {
                if (StringUtil.startsWithIgnoreCase(subCommand, lastWord)) {
                    suggestion.add(subCommand);
                }
            }

            Collections.sort(suggestion, String.CASE_INSENSITIVE_ORDER);
            return suggestion;
        }

        final String subCommand = args[0];
        final CommandHandler commandHandler = commands.get(subCommand);
        if (commandHandler != null) {
            suggestion = commandHandler.onTabComplete(sender, subCommand, Arrays.copyOfRange(args, 1, args.length));
            if (suggestion != null) {
                Collections.sort(suggestion, String.CASE_INSENSITIVE_ORDER);
            }

            return suggestion;
        }

        return null;
    }

    private void registerCommands() {
        register(new ToggleCommand(plugin));
        register(new InfoCommand(plugin));
        register(new HelpCommand(plugin, this));
        register(new ToggleCommand(plugin));
    }

    private void register(CommandHandler handler) {
        for (String alias : handler.getAliases()) {
            commands.put(alias, handler);
            subCommands.add(alias);
        }

        final String subCommand = handler.getSubCommand();
        commands.put(subCommand, handler);
        subCommands.add(subCommand);
    }
}
