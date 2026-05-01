# Data Handler for Wildlife Conflict Management

class DataHandler:
    def __init__(self):
        self.wildlife_conflicts = []

    def add_conflict(self, conflict):
        self.wildlife_conflicts.append(conflict)
        print('Conflict added: ', conflict)

    def get_conflicts(self):
        return self.wildlife_conflicts

    def clear_conflicts(self):
        self.wildlife_conflicts.clear()
        print('All conflicts cleared.')