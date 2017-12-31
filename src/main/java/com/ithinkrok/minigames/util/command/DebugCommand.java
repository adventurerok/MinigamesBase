package com.ithinkrok.minigames.util.command;

import com.ithinkrok.minigames.api.Kit;
import com.ithinkrok.minigames.api.command.MinigamesCommand;
import com.ithinkrok.minigames.api.command.MinigamesCommandSender;
import com.ithinkrok.minigames.api.event.MinigamesCommandEvent;
import com.ithinkrok.minigames.api.item.CustomItem;
import com.ithinkrok.minigames.api.team.Team;
import com.ithinkrok.minigames.api.user.User;
import com.ithinkrok.minigames.util.metadata.Money;
import com.ithinkrok.msm.common.economy.Account;
import com.ithinkrok.msm.common.economy.Currency;
import com.ithinkrok.msm.common.economy.result.Balance;
import com.ithinkrok.util.event.CustomEventHandler;
import com.ithinkrok.util.event.CustomListener;
import com.ithinkrok.util.math.ExpressionCalculator;

import java.math.BigDecimal;
import java.util.*;

public class DebugCommand implements CustomListener {

    private final Map<String, SubExecutorInfo> subExecutors = new HashMap<>();

    public DebugCommand() {
        addSubExecutor("custom", "mg.base.debug.custom", this::customCommand);
        addSubExecutor("level", "mg.base.debug.level", this::levelCommand);
        addSubExecutor("team", "mg.base.debug.team", this::teamCommand);
        addSubExecutor("kit", "mg.base.debug.kit", this::kitCommand);
        addSubExecutor("money", "mg.base.debug.money", this::moneyCommand);
        addSubExecutor("customlist", "mg.base.debug.customlist", this::customListCommand);
        addSubExecutor("econ", "mg.base.debug.econ", this::econCommand);
    }

    private boolean econCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if(!command.requireArgumentCount(sender, 1)) return false;
        if(!command.requireUser(sender)) return false;

        User user = command.getUser();

        String currencyName = command.getStringArg(0, null);
        Account account = user.getEconomyAccount();
        Currency currency = account.lookupCurrency(currencyName);

        if(currency == null) {
            sender.sendLocale("command.debug.econ.unknown", currencyName);
            return true;
        }

        sender.sendLocale("command.debug.econ.title", currencyName);
        sender.sendLocale("command.debug.econ.type", currency.getCurrencyType());
        sender.sendLocale("command.debug.econ.names", currency.getFormattedName());
        sender.sendLocale("command.debug.econ.decimals", currency.getDecimalPlaces());

        Optional<Balance> optBalance = account.getBalance(currency);
        if(optBalance.isPresent()) {
            sender.sendLocale("command.debug.econ.optbalance.present",
                              currency.format(optBalance.get().getAmount()));
        } else {
            sender.sendLocale("command.debug.econ.optbalance.notpresent");
        }

        BigDecimal nonFinalChange = BigDecimal.ZERO;

        if(command.hasArg(1)) {
            nonFinalChange = new BigDecimal(command.getStringArg(1, null));
        }

        BigDecimal change = nonFinalChange;

        account.getBalance(currency, balance -> {
            sender.sendLocale("command.debug.econ.balance",
                              currency.format(balance.getAmount()));

            int compare = change.compareTo(BigDecimal.ZERO);
            if(compare > 0) {
                account.deposit(currency, change, "debug econ deposit", result -> {
                    if(result == null) {
                        sender.sendLocale("command.debug.econ.nullresult");
                        return;
                    }

                    sender.sendLocale("command.debug.econ.deposit",
                                      result.getTransactionResult(),
                                      currency.format(result.getBalanceChange().getNewBalance()));
                });
            } else if(compare < 0) {
                account.withdraw(currency, change.negate(), "debug econ withdraw", result -> {
                    if(result == null) {
                        sender.sendLocale("command.debug.econ.nullresult");
                        return;
                    }

                    sender.sendLocale("command.debug.econ.withdraw",
                                      result.getTransactionResult(),
                                      currency.format(result.getBalanceChange().getNewBalance()));
                });
            }
        });

        return true;
    }

    protected void addSubExecutor(String name, String permission, SubCommandExecutor executor) {
        subExecutors.put(name, new SubExecutorInfo(executor, permission));
    }

    @CustomEventHandler
    public void onCommand(MinigamesCommandEvent event) {
        MinigamesCommandSender sender = event.getCommandSender();
        MinigamesCommand command = event.getCommand();

        if (!command.requireArgumentCount(sender, 1)) {
            event.setValidCommand(false);
            return;
        }

        MinigamesCommand subCommand = command.subCommand();

        if (!subExecutors.containsKey(subCommand.getCommand())) {
            sender.sendLocale("command.debug.unknown", subCommand.getCommand());
            return;
        }

        SubExecutorInfo executorInfo = subExecutors.get(subCommand.getCommand());

        if (!MinigamesCommand.requirePermission(sender, executorInfo.permission)) return;

        boolean validCommand = executorInfo.executor.onCommand(sender, subCommand);
        event.setValidCommand(validCommand);
    }

    private boolean moneyCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if (!command.requireUser(sender)) return true;

        int amount = command.getIntArg(0, 10000);

        Money userMoney = Money.getOrCreate(command.getUser());
        userMoney.addMoney(amount, true);

        if (command.getUser().getTeam() == null) return true;
        int teamAmount = command.getIntArg(1, amount);

        Money teamMoney = Money.getOrCreate(command.getUser().getTeam());
        teamMoney.addMoney(teamAmount, true);

        return true;
    }

    private boolean teamCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if (!command.requireUser(sender)) return true;
        if (!command.requireArgumentCount(sender, 1)) return false;

        Team team = command.getGameGroup().getTeam(command.getStringArg(0, null));
        if (team == null) {
            sender.sendLocale("command.debug.team.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().setTeam(team);
        sender.sendLocale("command.debug.team.success", command.getUser().getFormattedName(), team.getFormattedName());

        return true;
    }

    private boolean customCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if (!command.requireUser(sender)) return true;
        if (!command.requireArgumentCount(sender, 1)) return false;

        CustomItem item = command.getUser().getGameGroup().getCustomItem(command.getStringArg(0, null));
        if (item == null) {
            sender.sendLocale("command.debug.custom.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().getInventory().addItem(item.createForUser(command.getUser()));
        sender.sendLocale(
                "command.debug.custom.success",
                command.getUser().getFormattedName(),
                command.getStringArg(0, null));

        return true;
    }

    private boolean customListCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        Collection<CustomItem> allCustoms = command.getUser().getGameGroup().getAllCustomItems();

        List<CustomItem> customList = new ArrayList<>(allCustoms);
        customList.sort(Comparator.comparing(CustomItem::getName));

        sender.sendLocaleNoPrefix("command.debug.customlist.title");

        String separator = sender.getLanguageLookup().getLocale("command.debug.customlist.separator");
        StringBuilder current = new StringBuilder();

        for(int index = 0; index < customList.size(); ++index) {
            String itemName = customList.get(index).getName();

            if(current.length() == 0 || current.length() + itemName.length() < 60) {
                current.append(itemName);
            } else {
                sender.sendLocaleNoPrefix("command.debug.customlist.line", current.toString());
                current = new StringBuilder();
                current.append(itemName);
            }

            if(index != customList.size() - 1) {
                current.append(separator);
            }
        }

        if(current.length() > 0) {
            sender.sendLocaleNoPrefix("command.debug.customlist.line", current.toString());
        }

        return true;
    }

    private boolean levelCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if (!command.requireUser(sender)) return true;
        if (!command.requireArgumentCount(sender, 2)) return false;

        String upgrade = command.getStringArg(0, null);
        int level = (int) new ExpressionCalculator(
                command.getStringArg(1, "0")
        ).calculate(command.getUser().getUserVariables());

        command.getUser().setUserVariable(upgrade, level);
        sender.sendLocale("command.debug.level.success", command.getUser().getFormattedName(), upgrade, level);

        return true;
    }

    private boolean kitCommand(MinigamesCommandSender sender, MinigamesCommand command) {
        if (!command.requireUser(sender)) return true;
        if (!command.requireArgumentCount(sender, 1)) return false;

        Kit kit = command.getGameGroup().getKit(command.getStringArg(0, null));
        if (kit == null) {
            sender.sendLocale("command.debug.kit.unknown", command.getStringArg(0, null));
            return true;
        }

        command.getUser().setKit(kit);

        sender.sendLocale("command.debug.kit.success", command.getUser().getFormattedName(), kit.getFormattedName());

        return true;
    }

    protected interface SubCommandExecutor {

        boolean onCommand(MinigamesCommandSender sender, MinigamesCommand command);
    }

    private static class SubExecutorInfo {
        SubCommandExecutor executor;
        String permission;

        public SubExecutorInfo(SubCommandExecutor executor, String permission) {
            this.executor = executor;
            this.permission = permission;
        }
    }
}
