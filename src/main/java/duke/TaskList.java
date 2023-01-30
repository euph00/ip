package duke;

import duke.exceptions.DukeException;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.ToDo;
import duke.ui.Ui;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class handles the tracking of added <code>Task</code>s while the bot is running. This is an abstraction of
 * a list of tasks to be used by the bot.
 */
public class TaskList {
    final List<Task> tasks;

    /**
     * Standard constructor for an instance of TaskList.
     */
    TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Method to accept tokens that were parsed by <code>Parser</code> which read existing data from stored file on disk
     * and adds the contents into the current session.
     * @param tokens <code>String[]</code> of tokens as read from the data file by <code>Parser</code>.
     */
    void parseEventFromFile(String[] tokens) {
        String taskType = tokens[0];
        if (Objects.equals(taskType, "T")) {
            tasks.add(new ToDo(tokens[2], tokens[1]));
        } else if (Objects.equals(taskType, "D")) {
            tasks.add(new Deadline(tokens[2], tokens[3], tokens[1]));
        } else if (Objects.equals(taskType, "E")) {
            tasks.add(new Event(tokens[2], tokens[3], tokens[4], tokens[1]));
        }
    }

    /**
     * Method to handle adding a task to the underlying list, and trigger corresponding <code>Ui</code> event.
     * @param task Target <code>Task</code> to be added to list.
     */
    void addTaskToList(Task task) {
        this.tasks.add(task);
    }

    /**
     * Method to check the validity of the format of a <code>ToDo</code> task, and adds it to the list.
     * @param tokens <code>String[]</code> of arguments from <code>Parser</code>.
     * @throws DukeException In the event that the name of the task is not specified.
     */
    Task addToDo(String[] tokens) throws DukeException {
        StringBuilder sb = new StringBuilder();
        if (tokens.length < 2) {
            throw new DukeException("The description of a todo cannot be empty");
        }
        for (int i = 1; i < tokens.length; i++) {
            sb.append(tokens[i]).append(" ");
        }
        sb.deleteCharAt(sb.length()-1);
        ToDo td = new ToDo(sb.toString());
        addTaskToList(td);
        return td;
    }

    /**
     * Method to check the validity of the format of a <code>DeadLine</code> task, and adds it to the list.
     * @param tokens <code>String[]</code> of arguments from <code>Parser</code>.
     * @throws DukeException In the event that the name of the task is not specified, or the deadline is not specified
     * in the correct format with <code>/by</code> tag.
     */
    Task addDeadline(String[] tokens) throws DukeException {
        StringBuilder sb = new StringBuilder();
        int idxDelimiter = Arrays.asList(tokens).indexOf("/by");
        if (idxDelimiter == -1) {
            throw new DukeException("deadline tasks must be specified by /by [deadline] format");
        } else if (idxDelimiter == tokens.length - 1) {
            throw new DukeException("please specify a deadline after the /by tag");
        } else if (idxDelimiter == 1) {
            throw new DukeException("The description of a deadline cannot be empty");
        } else {
            for (int i = 1; i < idxDelimiter; i++) {
                sb.append(tokens[i]).append(" ");
            }
            String taskName = sb.deleteCharAt(sb.length()-1).toString();
            sb.delete(0, sb.length());
            for (int i = idxDelimiter + 1; i < tokens.length; i++) {
                sb.append(tokens[i]).append(" ");
            }
            String dueDate = sb.deleteCharAt(sb.length()-1).toString();
            try {
                Task task = new Deadline(taskName, dueDate);
                addTaskToList(task);
                return task;
            } catch (DateTimeParseException e) {
                throw new DukeException("Please enter a valid date in the format YYYY-MM-DD/HH:mm\n");
            }
        }
    }

    /**
     * Method to check the validity of the format of a <code>DeadLine</code> task, and adds it to the list.
     * @param tokens <code>String[]</code> of arguments from <code>Parser</code>.
     * @param ui Instance of <code>Ui</code> associated with the calling instance of <code>Duke</code>.
     * @throws DukeException In the event that the name of the task is not specified, or the from and to date times are
     * not specified in the correct format with <code>/from</code> and <code>/to</code> tags.
     */
    Task addEvent(String[] tokens, Ui ui) throws DukeException {
        StringBuilder sb = new StringBuilder();
        int idxFrom = Arrays.asList(tokens).indexOf("/from");
        int idxTo = Arrays.asList(tokens).indexOf("/to");
        if (idxFrom == -1 || idxTo == -1) {
            throw new DukeException("event tasks must be specified by a /from [start] /to [end] format");
        } else if (idxFrom > idxTo) {
            throw new DukeException("/to flag must come after /from flag");
        } else if (idxFrom == 1) {
            throw new DukeException("The description of a event task cannot be empty");
        } else if (idxTo - idxFrom == 1) {
            throw new DukeException("please specify a start datetime after /from flag");
        } else if (tokens.length - 1 == idxTo) {
            throw new DukeException("please specify an end datetime after /to flag");
        } else {
            for (int i = 1; i < idxFrom; i++) {
                sb.append(tokens[i]).append(" ");
            }
            String taskName = sb.deleteCharAt(sb.length()-1).toString();
            sb.delete(0, sb.length());

            for (int i = idxFrom + 1; i < idxTo; i++) {
                sb.append(tokens[i]).append(" ");
            }
            String taskFrom = sb.deleteCharAt(sb.length()-1).toString();
            sb.delete(0, sb.length());

            for (int i = idxTo + 1; i < tokens.length; i++) {
                sb.append(tokens[i]).append(" ");
            }
            String taskTo = sb.deleteCharAt(sb.length()-1).toString();
            try {
                Task task = new Event(taskName, taskFrom, taskTo);
                addTaskToList(task);
                return task;
            } catch (DateTimeParseException e) {
                throw new DukeException("Please enter valid dates in the format YYYY-MM-DD/HH:mm\n");
            }
        }
    }

    /**
     * Method to mark a list item and trigger corresponding <code>Ui</code> event.
     * @param tokens <code>String[]</code> of arguments from <code>Parser</code>, specifying the index to mark.
     * @param ui Instance of <code>Ui</code> associated with the calling instance of <code>Duke</code>.
     */
    Task markListItem(String[] tokens, Ui ui) throws DukeException {
        try {
            int listIndex = Integer.parseInt(tokens[1])-1;
            tasks.get(listIndex).setStatus("X");
            return tasks.get(listIndex);
        } catch (NumberFormatException e) {
            throw new DukeException("Please specify a numerical task index to mark\n");
        } catch (IndexOutOfBoundsException e) {
            throw new DukeException("Please specify a valid index to mark\n");
        }
    }

    /**
     * Method to unmark a list item and trigger corresponding <code>Ui</code> event.
     * @param tokens <code>String[]</code> of arguments from <code>Parser</code>, specifying the index to unmark.
     * @param ui Instance of <code>Ui</code> associated with the calling instance of <code>Duke</code>.
     */
    Task unmarkListItem(String[] tokens, Ui ui) throws DukeException {
        try {
            int listIndex = Integer.parseInt(tokens[1]) - 1;
            tasks.get(listIndex).setStatus(" ");
            return tasks.get(listIndex);
        } catch (NumberFormatException e) {
            throw new DukeException("Please specify a numerical task index to unmark\n");
        } catch (IndexOutOfBoundsException e) {
            throw new DukeException("Please specify a valid index to unmark\n");
        }
    }

    /**
     * Method to delete an item from the list and trigger corresponding <code>Ui</code> event.
     * @param tokens tokens <code>String[]</code> of arguments from <code>Parser</code>, specifying the index to delete.
     * @param ui Instance of <code>Ui</code> associated with the calling instance of <code>Duke</code>.
     * @throws DukeException In the event that the specified list index is out of bounds, or the argument corresponding
     * to the deletion index is not an integer.
     */
    Task deleteItem(String[] tokens, Ui ui) throws DukeException {
        if (tokens.length != 2) {
            throw new DukeException("please specify delete command as delete [list index]");
        } else if (tasks.size() == 0) {
            throw new DukeException("Task list is empty");
        }
        try {
            int listIndex = Integer.parseInt(tokens[1]) - 1;
            return tasks.remove(listIndex);
        } catch (NumberFormatException e) {
            throw new DukeException("please specify a valid number to delete entry\n");
        } catch (IndexOutOfBoundsException e) {
            throw new DukeException("please specify a valid index to delete\n");
        }
    }

    /**
     * Method to find all items containing keyword in the list, and invokes the associated Ui event to
     * display matching items
     * @param tokens <code>String[]</code> provided by <code>Parser</code>.
     * @throws DukeException In the event that no keyword is specified.
     */
    List<Integer> getMatchingItemsIndices(String[] tokens) throws DukeException {
        if (tokens.length == 1) {
            throw new DukeException("please provide a keyword or keywords to search for");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < tokens.length; i++) {
            sb.append(tokens[i]).append(' ');
        }
        sb.deleteCharAt(sb.length()-1);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (task.getName().matches(".*" + sb + ".*")) {
                indices.add(i);
            }
        }
        return indices;
    }

    /**
     * Method to format and get all current tasks in the <code>TaskList</code>.
     */
    public String getItemListAsResponseString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the tasks in your list:\n");
        for (int i = 0; i < tasks.size(); i++) {
            sb
                    .append(i+1)
                    .append(".")
                    .append(tasks.get(i).toString())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to get the <code>Task</code> items at the indices specified.
     * @param indices <code>List<Integer></code> containing desired indices to be obtained
     */
    public String getItemListAsResponseString(List<Integer> indices) {
        StringBuilder sb = new StringBuilder();
        sb.append("Here are the wanted tasks in your list:\n");
        for (int i : indices) {
            sb
                    .append(i+1)
                    .append(".")
                    .append(tasks.get(i).toString())
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns number of elements in the list.
     * @return <code>int</code> representing number of elements in the list.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the item at specified index in the list.
     * @param idx Index of desired item.
     * @return <code>Task</code> object that is at the index specified.
     */
    public Task get(int idx) {
        return tasks.get(idx);
    }
}
