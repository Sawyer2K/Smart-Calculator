package calculator;

import java.util.*;
import java.math.BigInteger;

public class Main {
    private static final Map<String, BigInteger> variablesMap = new HashMap<>();
    private static final String EXIT_COMMAND = "/exit";
    private static final String HELP_COMMAND = "/help";

    private static void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine().trim();

            InputType currentInput = checkInput(input);

            switch (currentInput) {
                case COMMAND:
                    handleCommand(input);
                    break;
                case EXPRESSION:
                    Queue<String> parsedExpression = parseToRPN(parseInputLineToArray(input));
                    System.out.println(evaluatingExpression(parsedExpression));
                    break;
                case VAR_INIT:
                    initializationVariable(input);
                    break;
                case SINGLE_VAR:
                    printVariable(input);
                    break;
                case SINGLE_NUMBER:
                    printSingleNumber(input);
                    break;
                case EMPTY_LINE:
                    continue;
                case ERROR:
                    System.out.println("Invalid expression");
                    break;
            }
        }
    }

    private static boolean isCorrectBraces(String input) {
        Deque<Character> brackets = new ArrayDeque<>();

        char[] arrayOfBrackets = input.toCharArray();
        boolean flag = true;

        for (char ch : arrayOfBrackets) {
            switch (ch) {
                case '(':
                case '{':
                case '[':
                    brackets.offerLast(ch);
                    break;
                case ')':
                    if (brackets.isEmpty() || brackets.peekLast() != '(') {
                        return false;
                    } else {
                        brackets.pollLast();
                    }
                    break;
                case '}':
                    if (brackets.isEmpty() || brackets.peekLast() != '{') {
                        return false;
                    } else {
                        brackets.pollLast();
                    }
                    break;
                case ']':
                    if (brackets.isEmpty() || brackets.peekLast() != '[') {
                        return false;
                    } else {
                        brackets.pollLast();
                    }
                    break;
                default:
                    break;
            }
        }

        if (!brackets.isEmpty()) {
            flag = false;
        }

        return flag;
    }

    private static InputType checkInput(String input) {
        String expressionRegex = "(([-+\\dA-Za-z()]+?[-+?\\s]+?|[*/^]\\s+?)+?[\\dA-Za-z()]+)";
        String commandRegex = "(/.+?)\\Z";
        String singleNumberRegex = "\\s*?[-+]?\\d+";
        String singleVarRegex = "\\s*?[A-Za-z]+?";
        String variableInitialization = "\\s*?\\w+?\\s*?=\\s*?[-+]*?\\w+?\\s*?";

        if (input.matches(commandRegex)) {
            return InputType.COMMAND;
        } else if (input.matches(expressionRegex)) {
            return isCorrectBraces(input) ? InputType.EXPRESSION : InputType.ERROR;
        } else if (input.matches(variableInitialization)) {
            return InputType.VAR_INIT;
        } else if(input.matches(singleVarRegex)) {
            return InputType.SINGLE_VAR;
        } else if (input.matches(singleNumberRegex)) {
            return InputType.SINGLE_NUMBER;
        } else if (input.isEmpty()) {
            return InputType.EMPTY_LINE;
        } else {
            return InputType.ERROR;
        }
    }

    private static void handleCommand(String input) {
        switch (input) {
            case EXIT_COMMAND:
                System.out.println("Bye!");
                System.exit(0);
                break;
            case HELP_COMMAND:
                printHelp();
                break;
            default:
                System.out.println("Unknown command");
                break;
        }
    }

    private static void printHelp() {
        System.out.println("The calculator supports addition, subtraction, multiplication,\n" +
                "division, exponentiation, and variable assignment using \"=\". \n" +
                "Variables are case-sensitive and must only consist of Latin characters.\n" +
                "This calculator is just written to practice Java. It does not catch all \n" +
                "possible bad combinations of inputs. The main goal is to pass all tests.\n" +
                "Type \"/exit\" to quit.");
    }

    private static void printVariable(String input) {
        if (variablesMap.containsKey(input)) {
            System.out.println(variablesMap.get(input));
        } else {
            System.out.println("Unknown variable");
        }
    }

    private static void printSingleNumber(String input) {
        System.out.println(input.charAt(0) == '+' ? input.substring(1) : input);
    }

    private static void initializationVariable(String input) {
        String[] inputArr = input.split("=");
        String identifier = inputArr[0].trim();
        String assignment = inputArr[1].trim();

        if (!isCorrectInitialization(identifier, assignment)) {
            return;
        }

        if (isNumber(assignment)) {
            variablesMap.put(identifier, new BigInteger(assignment));
        } else {
            if (variablesMap.containsKey(assignment)) {
                variablesMap.put(identifier, variablesMap.get(assignment));
            } else {
                System.out.println("Unknown variable");
            }
        }
    }

    private static boolean isCorrectInitialization(String identifier, String assignment) {
        if (!identifier.matches("\\s*?[A-Za-z]+?")) {
            System.out.println("Invalid identifier");
            return false;
        } else if (!assignment.matches("([A-Za-z]+?)|([-+]*?\\d+?)")) {
            System.out.println("Invalid assignment");
            return false;
        }

        return true;
    }

    private static boolean isNumber(String valueOfVariable) {
        return valueOfVariable.matches("[-=]*?\\d+?");
    }

    private static ArrayList<String> parseInputLineToArray(String inputLine) {
        ArrayList<String> array = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (char litter : inputLine.toCharArray()) {
            if (Character.isDigit(litter) || Character.isLetter(litter)) {
                builder.append(litter);
            } else if (builder.length() > 0) {
                array.add(builder.toString());
                builder = new StringBuilder();
            }

            switch (litter) {
                case ' ':
                    continue;
                case '+':
                case '-':
                case '*':
                case '/':
                case '^':
                case '(':
                case ')':
                    array.add(" ");
                    array.add(String.valueOf(litter));
                    array.add(" ");
                    break;
            }
        }

        if (builder.length() > 0) {
            array.add(builder.toString());
        }

        return trimPlusAndMinus(array);
    }

    private static Queue<String> parseToRPN(ArrayList<String> array) {
        //String[] array = inputLine.split("\\s+");

        Queue<String> queue = new ArrayDeque<>();
        Deque<String> stack = new ArrayDeque<>();
        Map<String, Integer> operatorsPriority = new HashMap<>();
        operatorsPriority.put("^", 4);
        operatorsPriority.put("*", 3);
        operatorsPriority.put("/", 3);
        operatorsPriority.put("-", 2);
        operatorsPriority.put("+", 2);
        operatorsPriority.put("(", 1);

        for (String currentToken : array) {
            switch (currentToken) {
                case " ":
                    continue;
                case "+":
                case "-":
                case "/":
                case "*":
                case "^":
                    if (stack.isEmpty() || stack.peekLast().equals("(")) {
                        stack.offerLast(currentToken);
                    } else if (operatorsPriority.get(currentToken) > operatorsPriority.get(stack.peekLast())) {
                        stack.offerLast(currentToken);
                    } else {
                        while (!stack.isEmpty() && operatorsPriority.get(stack.peekLast()) >= operatorsPriority.get(currentToken)) {
                            queue.offer(stack.pollLast());
                        }
                        stack.offerLast(currentToken);
                    }
                    break;
                case "(":
                    stack.offerLast(currentToken);
                    break;
                case ")":
                    while (!Objects.equals(stack.peekLast(), "(")) {
                        queue.offer(stack.pollLast());
                    }
                    stack.pollLast();
                    break;
                default:
                    if (isNumber(currentToken)) {
                        queue.offer(currentToken);
                    } else {
                        queue.offer(String.valueOf(variablesMap.get(currentToken)));
                    }
                    break;
            }
        }

        while (!stack.isEmpty()) {
            queue.offer(stack.pollLast());
        }

        return queue;
    }

    private static ArrayList<String> trimPlusAndMinus(ArrayList<String> parsedArray) {
        ArrayList<String> trimmedArray = new ArrayList<>();
        boolean isPlus;
        StringBuilder minusCounter = new StringBuilder();


        for (String currentToken : parsedArray) {
            if (currentToken.matches("[A-Za-z\\d/*()^]+?")) {
                if (minusCounter.length() > 0) {
                    isPlus = checkCountOfMinus(minusCounter.toString());
                    trimmedArray.add(isPlus ? "+" : "-");
                    minusCounter = new StringBuilder();
                }
                trimmedArray.add(currentToken);
            } else if (currentToken.matches("[+-]")) {
                minusCounter.append(currentToken);
            }
        }

        return trimmedArray;
    }
    
    private static boolean checkCountOfMinus(String token) {
        int minusCount = 0;
        
        for (char ch : token.toCharArray()) {
            if (ch == '-') {
                minusCount++;
            }
        }

        return minusCount % 2 == 0;
    }

    private static BigInteger evaluatingExpression(Queue<String> parsedExpression) {
        Deque<BigInteger> stack = new ArrayDeque<>();

        for (String item : parsedExpression) {
            if (isNumber(item)) {
                stack.offerLast(new BigInteger(item));
            } else if (item.matches("[A-Za-z]+?")) {
                stack.offerLast(variablesMap.get(item));
            } else {
                BigInteger secondOperand = stack.removeLast();
                BigInteger firstOperand = stack.removeLast();

                switch (item) {
                    case "*":
                        stack.offerLast(firstOperand.multiply(secondOperand));
                        break;
                    case "/":
                        stack.offerLast(firstOperand.divide(secondOperand));
                        break;
                    case "+":
                        stack.offerLast(firstOperand.add(secondOperand));
                        break;
                    case "-":
                        stack.offerLast(firstOperand.subtract(secondOperand));
                        break;
                }
            }
        }

        return stack.getLast();
    }

    enum InputType {
        COMMAND,
        EMPTY_LINE,
        VAR_INIT,
        SINGLE_VAR,
        SINGLE_NUMBER,
        EXPRESSION,
        ERROR
    }

    public static void main(String[] args) {

        run();
    }
}

