class MessageStructure:
    header = None
    message = None

    def __init__(self, h, m):
        self.header = h
        self.message = m

    def getHeader(self):
        return self.header

    def getMessage(self):
        return self.message