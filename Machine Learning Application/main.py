import kivy
from kivy.app import App
from kivy.uix.widget import Widget
from kivy.lang import Builder

Builder.load_file('main.kv')

import predictor


class MyGridLayout(Widget):
    def press(self):
        q_pred = predictor.QuadPredictor('test.wav')
        self.ids.lblOutput.text = q_pred.output_text


class Auralyze(App):
    def build(self):
        return MyGridLayout()

if __name__ == '__main__':
    app = Auralyze()
    app.run()